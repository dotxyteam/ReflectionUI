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
import javax.swing.SwingUtilities;

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
	protected Component nullStatusControl;
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
		nullStatusControl = createNullStatusControl();
		if (!data.isGetOnly()) {
			add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.NORTH), BorderLayout.WEST);
		}
		refreshUI();
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setNullStatusControlState(boolean b) {
		((JCheckBox) nullStatusControl).setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !((JCheckBox) nullStatusControl).isSelected();
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
			data.setValue(generateNonNullValue());
		}
		refreshUI();
	}

	protected Object generateNonNullValue() {
		Object result = null;
		try {
			result = this.swingRenderer.onTypeInstanciationRequest(this, input.getControlData().getType(), false);
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(this, t);
		}
		return result;
	}

	public void updateSubControl(Object newValue) {
		if (newValue != null) {
			ITypeInfo newValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
			if (newValueType.equals(subControlValueType)) {
				if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
					swingRenderer.refreshAllFieldControls((JPanel) subControl, false);
					return;
				}
			}
		}
		if (subControl != null) {
			remove(subControl);
		}
		if (newValue == null) {
			subControlValueType = null;
			subControl = createNullControl();
		} else {
			subControlValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
			subControl = createSubControl();
		}

		add(subControl, BorderLayout.CENTER);
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	protected Component createNullStatusControl() {
		JCheckBox result = new JCheckBox();
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});
		result.setVisible(!data.isGetOnly());
		return result;
	}

	protected Component createNullControl() {
		NullControl result = new NullControl(swingRenderer, input);
		if (!data.isGetOnly()) {
			result.setAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		}
		return result;
	}

	protected Component createSubControl() {
		final JPanel result = new AbstractEditorPanelBuilder() {

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullable() {
				return false;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, input.getModificationsTarget());
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public String getEditorTitle() {
				return ReflectionUIUtils.composeMessage(data.getType().getCaption(), "Dynamic Wrapper");
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				IInfoFilter result = DesktopSpecificProperty
						.getFilter(DesktopSpecificProperty.accessControlDataProperties(data));
				if (result == null) {
					result = IInfoFilter.NO_FILTER;
				}
				return result;
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return subControlValueType;
			}

			@Override
			public String getEncapsulationTypeCaption() {
				return ReflectionUIUtils.composeMessage("Nullable", subControlValueType.getCaption());
			}

			@Override
			public ModificationStack getParentModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Object getInitialObjectValue() {
				return data.getValue();
			}
		}.createEditorPanel();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingRenderer.getFieldControlPlaceHolders(result).get(0).getFieldControl().requestFocusInWindow();
			}
		});
		return result;
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
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
		if (!SwingRendererUtils.isForm(subControl, swingRenderer)) {
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
		if (!SwingRendererUtils.isForm(subControl, swingRenderer)) {
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
		if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
			swingRenderer.validateForm((JPanel) subControl);
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}
}