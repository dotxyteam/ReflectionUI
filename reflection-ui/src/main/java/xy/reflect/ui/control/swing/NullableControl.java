package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullableControl extends JPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IFieldControlData data;
	protected JCheckBox nullStatusControl;
	protected Component subControl;
	protected IFieldControlInput input;
	protected ITypeInfo subControlValueType;

	public NullableControl(SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = new JCheckBox();
		nullStatusControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
					subControl.requestFocusInWindow();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});
		nullStatusControl.setEnabled(!data.isGetOnly());
		add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.NORTH), BorderLayout.WEST);
		refreshUI();
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setNullStatusControlState(boolean b) {
		nullStatusControl.setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !nullStatusControl.isSelected();
	}

	@Override
	public boolean refreshUI() {
		Object value = data.getValue();
		setNullStatusControlState(value == null);
		updateSubControl(value);
		return true;
	}

	protected void onNullingControlStateChange() {
		if (getNullStatusControlState()) {
			data.setValue(null);
		} else {
			Object newValue = null;
			try {
				newValue = this.swingRenderer.onTypeInstanciationRequest(this, input.getControlData().getType(), false);
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(this, t);
			}
			data.setValue(newValue);
		}
		refreshUI();
	}

	public void updateSubControl(Object newValue) {
		if (newValue != null) {
			ITypeInfo newValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
			if (newValueType.equals(subControlValueType)) {
				swingRenderer.refreshAllFieldControls((JPanel) subControl, false);
				return;
			}
		}
		if (subControl instanceof NullControl) {
			if (newValue == null) {
				return;
			}
		}
		if (subControl != null) {
			remove(subControl);
		}
		if (newValue == null) {
			subControlValueType = null;
			subControl = new NullControl(swingRenderer, input);
			((NullControl) subControl).setAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		} else {
			subControlValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
			subControl = createSubControl();
		}

		add(subControl, BorderLayout.CENTER);
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	protected JPanel createSubControl() {
		return new AbstractSubObjectUIBuilber() {

			@Override
			public boolean isSubObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isSubObjectNullable() {
				return false;
			}

			@Override
			public boolean canCommitUpdatedSubObject() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createUpdatedSubObjectCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, input.getModificationsTarget());
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getSubObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public String getSubObjectTitle() {
				return ReflectionUIUtils.composeMessage(data.getType().getCaption(), "Dynamic Wrapper");
			}

			@Override
			public String getSubObjectModificationTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getSubObjectModificationTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public IInfoFilter getSubObjectFormFilter() {
				IInfoFilter result = DesktopSpecificProperty
						.getFilter(DesktopSpecificProperty.accessControlDataProperties(data));
				if (result == null) {
					result = IInfoFilter.NO_FILTER;
				}
				return result;
			}

			@Override
			public ITypeInfo getSubObjectDeclaredType() {
				return subControlValueType;
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Component getSubObjectOwnerComponent() {
				return NullableControl.this;
			}

			@Override
			public Object retrieveSubObjectValueFromParent() {
				return data.getValue();
			}
		}.createSubObjectForm();
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).displayError(msg);
		} else {
			return false;
		}
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		if (getNullStatusControlState() == true) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Object getFocusDetails() {
		if (subControl instanceof NullControl) {
			return null;
		}
		boolean subControlFocused = SwingRendererUtils.hasOrContainsFocus(subControl);
		Object subControlFocusDetails = swingRenderer.getFormFocusDetails((JPanel) subControl);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subControlFocused", subControlFocused);
		result.put("subControlFocusDetails", subControlFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		if (subControl instanceof NullControl) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Boolean subControlFocused = (Boolean) focusDetails.get("subControlFocused");
		Object subControlFocusDetails = focusDetails.get("subControlFocusDetails");
		if (Boolean.TRUE.equals(subControlFocused)) {
			if (subControlFocusDetails != null) {
				swingRenderer.requestFormDetailedFocus((JPanel) subControl, subControlFocusDetails);
			} else {
				subControl.requestFocusInWindow();
			}
		}
	}

	@Override
	public boolean requestFocusInWindow() {
		return subControl.requestFocusInWindow();
	}

	@Override
	public void validateSubForm() throws Exception {
		if (subControl instanceof IAdvancedFieldControl) {
			((IAdvancedFieldControl) subControl).validateSubForm();
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}
}