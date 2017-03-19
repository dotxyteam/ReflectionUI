package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
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
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class PolymorphicControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected List<ITypeInfo> subTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected JPanel dynamicControl;
	protected JPanel typeEnumerationControl;
	protected ITypeInfo polymorphicType;

	protected ITypeInfo lastInstanceType;
	protected IFieldControlInput input;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.polymorphicType = input.getControlData().getType();
		this.subTypes = polymorphicType.getPolymorphicInstanceSubTypes();

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(""));
		refreshUI();
	}

	protected JPanel createTypeEnumerationControl() {
		List<ITypeInfo> possibleTypes = new ArrayList<ITypeInfo>(subTypes);
		{
			if (polymorphicType.isConcrete()) {
				if (!possibleTypes.contains(polymorphicType)) {
					possibleTypes.add(polymorphicType);
				}
			}
			Object instance = data.getValue();
			if (instance != null) {
				ITypeInfo actualFieldValueType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
				if (!possibleTypes.contains(actualFieldValueType)) {
					instanceByEnumerationValueCache.put(actualFieldValueType, instance);
					possibleTypes.add(actualFieldValueType);
				}
			}
		}
		final ArrayAsEnumerationFactory enumFactory = ReflectionUIUtils
				.getPolymorphicTypesEnumerationfactory(swingRenderer.getReflectionUI(), polymorphicType, possibleTypes);
		final ITypeInfo enumType = swingRenderer.getReflectionUI().getTypeInfo(enumFactory.getInstanceTypeInfoSource());
		return new AbstractEditorPanelBuilder() {

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
				if (instance == null) {
					return null;
				} else {
					ITypeInfo actualFieldValueType = swingRenderer.getReflectionUI()
							.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
					instanceByEnumerationValueCache.put(actualFieldValueType, instance);
					return enumFactory.getInstance(actualFieldValueType);
				}
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return enumType;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return ValueReturnMode.COPY;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(final Object value) {
				try {
					Object instance;
					if (value == null) {
						instance = null;
					} else {
						ITypeInfo selectedPolyType = (ITypeInfo) enumFactory.unwrapInstance(value);
						instance = instanceByEnumerationValueCache.get(selectedPolyType);
						if (instance == null) {
							instance = swingRenderer.onTypeInstanciationRequest(PolymorphicControl.this,
									selectedPolyType, false);
							if (instance == null) {
								return IModification.NULL_MODIFICATION;
							}
						}
					}
					return new ControlDataValueModification(data, instance, input.getModificationsTarget());
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refreshDynamicControl();
						}
					});

				}
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

		}.createEditorPanel();
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected JPanel createDynamicControl(final ITypeInfo instanceType) {
		return new AbstractEditorPanelBuilder() {

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
			public String getEncapsulatedObjectTypeCaption() {
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

			@Override
			public EncapsulatedObjectFactory getEncapsulation() {
				EncapsulatedObjectFactory result = super.getEncapsulation();
				if (polymorphicType.equals(instanceType)) {
					Map<String, Object> fieldSpecificProperties = new HashMap<String, Object>(
							result.getFieldSpecificProperties());
					DesktopSpecificProperty.setPolymorphicControlForbidden(fieldSpecificProperties, true);
					result.setFieldSpecificProperties(fieldSpecificProperties);
				}
				return result;
			}

		}.createEditorPanel();
	}

	protected void refreshDynamicControl() {
		Object instance = data.getValue();
		ITypeInfo instanceType = null;
		if (instance != null) {
			instanceType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
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
				swingRenderer.refreshAllFieldControls(dynamicControl, false);
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

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			swingRenderer.refreshAllFieldControls(typeEnumerationControl, false);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	@Override
	public boolean refreshUI() {
		refreshTypeEnumerationControl();
		refreshDynamicControl();
		return true;
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
		return true;
	}

	@Override
	public Object getFocusDetails() {
		boolean typeEnumerationControlFocused = SwingRendererUtils.hasOrContainsFocus(typeEnumerationControl);
		Object typeEnumerationControlFocusDetails = null;
		if (typeEnumerationControlFocused) {
			typeEnumerationControlFocusDetails = swingRenderer.getFormFocusDetails(typeEnumerationControl);
		}
		boolean dynamicControlFocused = false;
		Object dynamicControlFocusDetails = null;
		if (dynamicControl != null) {
			dynamicControlFocused = SwingRendererUtils.hasOrContainsFocus(dynamicControl);
			if (dynamicControlFocused) {
				dynamicControlFocusDetails = swingRenderer.getFormFocusDetails(dynamicControl);
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("typeEnumerationControlFocused", typeEnumerationControlFocused);
		result.put("typeEnumerationControlFocusDetails", typeEnumerationControlFocusDetails);
		result.put("dynamicControlFocused", dynamicControlFocused);
		result.put("dynamicControlFocusDetails", dynamicControlFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		boolean typeEnumerationControlFocused = (Boolean) focusDetails.get("typeEnumerationControlFocused");
		Object typeEnumerationControlFocusDetails = focusDetails.get("typeEnumerationControlFocusDetails");
		boolean dynamicControlFocused = (Boolean) focusDetails.get("dynamicControlFocused");
		Object dynamicControlFocusDetails = focusDetails.get("dynamicControlFocusDetails");
		if (typeEnumerationControlFocused) {
			typeEnumerationControl.requestFocusInWindow();
			if (typeEnumerationControlFocusDetails != null) {
				swingRenderer.requestFormDetailedFocus(typeEnumerationControl, typeEnumerationControlFocusDetails);
			} else {
				typeEnumerationControl.requestFocusInWindow();
			}
		}
		if (dynamicControlFocused) {
			if (dynamicControl != null) {
				dynamicControl.requestFocusInWindow();
				if (dynamicControlFocusDetails != null) {
					swingRenderer.requestFormDetailedFocus(dynamicControl, dynamicControlFocusDetails);
				} else {
					dynamicControl.requestFocusInWindow();
				}
			}
		}
	}

	@Override
	public void validateSubForm() throws Exception {
		swingRenderer.validateForm(dynamicControl);
	}

}
