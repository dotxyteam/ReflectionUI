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

import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
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
	protected IControlData data;
	protected JCheckBox nullStatusControl;
	protected Component subControl;
	protected IControlInput input;
	protected ITypeInfo subControlValueType;

	public NullableControl(SwingRenderer swingRenderer, IControlInput input) {
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
					subControl.requestFocus();
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
		boolean hadFocus = (subControl != null) && SwingRendererUtils.hasOrContainsFocus(subControl);
		updateSubControl(value);
		if (hadFocus && (subControl != null)) {
			subControl.requestFocus();
		}
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
		boolean updated = false;
		if (subControl instanceof IAdvancedFieldControl) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) subControl;
			if (newValue != null) {
				if (fieldControl.refreshUI()) {
					updated = true;
				}
			}
		}
		if (!updated) {
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
	}

	protected Component createSubControl() {
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
			public IModification getUpdatedSubObjectCommitModification(Object newObjectValue) {
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
			public Object getInitialSubObjectValue() {
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
		Object subControlFocusDetails = null;
		Class<?> subControlClass = null;
		{
			if (subControl instanceof IAdvancedFieldControl) {
				subControlFocusDetails = ((IAdvancedFieldControl) subControl).getFocusDetails();
				subControlClass = subControl.getClass();
			}
		}
		if (subControlFocusDetails == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subControlFocusDetails", subControlFocusDetails);
		result.put("subControlClass", subControlClass);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Object subControlFocusDetails = focusDetails.get("subControlFocusDetails");
		Class<?> subControlClass = (Class<?>) focusDetails.get("subControlClass");
		subControl.requestFocus();
		if (subControl instanceof IAdvancedFieldControl) {
			if (subControl.getClass().equals(subControlClass)) {
				((IAdvancedFieldControl) subControl).requestDetailedFocus(subControlFocusDetails);
			}
		}
	}

	@Override
	public void requestFocus() {
		if (subControl != null) {
			subControl.requestFocus();
		}
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