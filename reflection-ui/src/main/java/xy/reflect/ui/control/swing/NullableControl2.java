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

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.FieldControlInputProxy;
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

public class NullableControl2 extends JPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IFieldControlData data;
	protected Component nullStatusControl;
	protected Component subControl;
	protected IFieldControlInput input;
	protected ITypeInfo subControlValueType;
	protected AbstractEditorPanelBuilder subFormBuilder;

	public NullableControl2(SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = createNullStatusControl();
		add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.CENTER), BorderLayout.WEST);
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
		setNullStatusControlState(data.getValue() == null);
		refreshSubControl();
		return true;
	}

	protected void onNullingControlStateChange() {
		Object newValue;
		if (getNullStatusControlState()) {
			newValue = null;
		} else {
			newValue = generateNonNullValue();
		}
		ReflectionUIUtils.setValueThroughModificationStack(data, newValue, input.getModificationStack(),
				input.getModificationsTarget());
		refreshUI();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				requestDetailedFocus(null);
			}
		});
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

	public void refreshSubControl() {
		Object value = data.getValue();
		if (value == null) {
			if (subControl != null) {
				if (subControlValueType == null) {
					return;
				}
			}
		} else {
			ITypeInfo newValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			if (newValueType.equals(subControlValueType)) {
				if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
					subFormBuilder.refreshEditorPanel((JPanel) subControl);
					return;
				}
			}
		}
		if (subControl != null) {
			remove(subControl);
		}
		if (value == null) {
			subControlValueType = null;
			subControl = createNullControl();
		} else {
			subControlValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			subControl = createSubForm();
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
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl2.this, t);
				}
			}
		});
		result.setEnabled(!data.isGetOnly());
		return result;
	}

	protected Component createNullControl() {
		NullControl2 result = new NullControl2(swingRenderer, new FieldControlInputProxy(input) {

			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public String getCaption() {
						return "";
					}

				};
			}

		});
		if (!data.isGetOnly()) {
			result.setAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		}
		result.setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return result;
	}

	protected Component createSubForm() {
		subFormBuilder = new AbstractEditorPanelBuilder() {

			@Override
			public String getEncapsulatedFieldCaption() {
				return "";
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectValueNullable() {
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
				return data.getType();
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
		};
		JPanel result = subFormBuilder.createEditorPanel(true);
		result.setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return result;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public Object getFocusDetails() {
		if (!SwingRendererUtils.isForm(subControl, swingRenderer)) {
			return null;
		}
		Object subControlFocusDetails = swingRenderer.getFormFocusDetails((JPanel) subControl);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subControlFocusDetails", subControlFocusDetails);
		return result;
	}

	@Override
	public boolean requestDetailedFocus(Object focusDetails) {
		if (focusDetails == null) {
			return SwingRendererUtils.requestAnyComponentFocus(subControl, null, swingRenderer);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) focusDetails;
		Object subControlFocusDetails = map.get("subControlFocusDetails");
		if (subControlFocusDetails != null) {
			return SwingRendererUtils.requestAnyComponentFocus(subControl, subControlFocusDetails, swingRenderer);
		}
		return false;
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

	@Override
	public String toString() {
		return "NullableControl2 [data=" + data + "]";
	}
}