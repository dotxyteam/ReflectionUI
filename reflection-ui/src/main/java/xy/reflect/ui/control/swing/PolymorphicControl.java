package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.GenericEnumerationFactory;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.util.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class PolymorphicControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected ITypeInfo polymorphicType;
	protected PolymorphicTypeOptionsFactory typeOptionsFactory;

	protected AbstractEditorPanelBuilder typeEnumerationControlBuilder;
	protected AbstractEditorPanelBuilder dynamicControlBuilder;
	protected JPanel dynamicControl;
	protected JPanel typeEnumerationControl;

	protected ITypeInfo lastInstanceType;
	protected IFieldControlInput input;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.polymorphicType = input.getControlData().getType();
		this.typeOptionsFactory = new PolymorphicTypeOptionsFactory(swingRenderer.getReflectionUI(), polymorphicType);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
		refreshUI();
	}

	protected JPanel createTypeEnumerationControl() {
		typeEnumerationControlBuilder = new AbstractEditorPanelBuilder() {

			ITypeInfo enumType = swingRenderer.getReflectionUI().getTypeInfo(typeOptionsFactory.getInstanceTypeInfoSource());
			Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();

			@Override
			public boolean isObjectFormExpanded() {
				return true;
			}

			@Override
			public boolean isObjectNullable() {
				return data.isNullable();
			}

			@Override
			public Object getInitialObjectValue() {
				Object instance = data.getValue();
				ITypeInfo selectedType = getSubType(instance);
				if (selectedType == null) {
					return null;
				}
				instanceByEnumerationValueCache.put(selectedType, instance);
				return typeOptionsFactory.getInstance(selectedType);
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return enumType;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return ValueReturnMode.CALCULATED;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			protected boolean isNewObjectValueAccepted(Object value) {
				Object instance;
				if (value != null) {
					ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
					instance = instanceByEnumerationValueCache.get(selectedSubType);
					if (instance == null) {
						instance = swingRenderer.onTypeInstanciationRequest(PolymorphicControl.this, selectedSubType,
								false);
						if (instance == null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									refreshTypeEnumerationControl();
								}
							});
							return false;
						}
						instanceByEnumerationValueCache.put(selectedSubType, instance);
					}
				}
				return true;
			}

			@Override
			public IModification createCommitModification(final Object value) {

				return new ModificationProxy(IModification.NULL_MODIFICATION) {

					@Override
					public String toString() {
						return "CommitModification [editor=PolymorphicControlEnumeration, data=" + data + "]";
					}

					@Override
					public IModification applyAndGetOpposite() {
						Object instance;
						if (value == null) {
							instance = null;
						} else {
							ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
							instance = instanceByEnumerationValueCache.get(selectedSubType);
							if (instance == null) {
								throw new ReflectionUIError();
							}
						}
						return new ControlDataValueModification(new FieldControlDataProxy(data) {
							@Override
							public void setValue(Object value) {
								try {
									super.setValue(value);
								} finally {
									refreshDynamicControl();
								}
							}
						}, instance, input.getModificationsTarget()).applyAndGetOpposite();
					}
				};
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public String getEditorTitle() {
				return ReflectionUIUtils.composeMessage(polymorphicType.getCaption(), "Polymorphic Type");
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
				return IInfoFilter.NO_FILTER;
			}

			@Override
			public ModificationStack getParentModificationStack() {
				return input.getModificationStack();
			}

		};
		return typeEnumerationControlBuilder.createEditorPanel();
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			typeEnumerationControlBuilder.refreshEditorPanel(typeEnumerationControl);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected ITypeInfo getSubType(Object instance) {
		if (instance == null) {
			return null;
		}
		for (ITypeInfo type : typeOptionsFactory.getTypeOptions()) {
			if (!type.getName().equals(polymorphicType.getName())) {
				if (type.supportsInstance(instance)) {
					return type;
				}
			}
		}
		for (ITypeInfo type : typeOptionsFactory.getTypeOptions()) {
			if (type.getName().equals(polymorphicType.getName())) {
				if (type.supportsInstance(instance)) {
					return type;
				}
			}
		}
		return null;
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected JPanel createDynamicControl(final ITypeInfo instanceType) {
		dynamicControlBuilder = new AbstractEditorPanelBuilder() {

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
				return instanceType;
			}

			@Override
			public String getEncapsulationTypeCaption() {
				return ReflectionUIUtils.composeMessage("Polymorphic", polymorphicType.getCaption());
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
		return dynamicControlBuilder.createEditorPanel();
	}

	protected void refreshDynamicControl() {
		Object instance = data.getValue();
		ITypeInfo instanceType = null;
		if (instance != null) {
			instanceType = getSubType(instance);
		}
		if ((lastInstanceType == null) && (instanceType == null)) {
			return;
		} else if ((lastInstanceType != null) && (instanceType == null)) {
			remove(dynamicControl);
			dynamicControl = null;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else if ((lastInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (lastInstanceType.equals(instanceType)) {
				dynamicControlBuilder.refreshEditorPanel(dynamicControl);
			} else {
				remove(dynamicControl);
				dynamicControl = null;
				dynamicControl = createDynamicControl(instanceType);
				add(dynamicControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
		}
		lastInstanceType = instanceType;
	}

	@Override
	public boolean refreshUI() {
		refreshTypeEnumerationControl();
		refreshDynamicControl();
		return true;
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
		Object dynamicControlFocusDetails = null;
		if (dynamicControl != null) {
			dynamicControlFocusDetails = swingRenderer.getFormFocusDetails(dynamicControl);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("dynamicControlFocusDetails", dynamicControlFocusDetails);
		return result;
	}

	@Override
	public boolean requestDetailedFocus(Object focusDetails) {
		if (focusDetails == null) {
			return SwingRendererUtils.requestAnyComponentFocus(typeEnumerationControl, null, swingRenderer);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) focusDetails;
		Object dynamicControlFocusDetails = map.get("dynamicControlFocusDetails");
		if (dynamicControlFocusDetails != null) {
			return SwingRendererUtils.requestAnyComponentFocus(dynamicControl, dynamicControlFocusDetails,
					swingRenderer);
		}
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
		swingRenderer.validateForm(dynamicControl);
	}

	@Override
	public String toString() {
		return "PolymorphicControl [data=" + data + "]";
	}

}
