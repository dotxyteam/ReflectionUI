package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class PolymorphicControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;

	protected List<ITypeInfo> subTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected Component dynamicControl;
	protected Component typeEnumerationControl;
	protected ITypeInfo polymorphicType;

	protected final ITypeInfo NULL_POLY_TYPE;
	protected ITypeInfo lastInstanceType;
	protected boolean updatingEnumeration = false;
	protected FieldControlPlaceHolder fieldControlPlaceHolder;

	public PolymorphicControl(final SwingRenderer swingRenderer, final IControlData data) {
		this.swingRenderer = swingRenderer;
		this.data = data;
		this.polymorphicType = data.getType();
		this.subTypes = polymorphicType.getPolymorphicInstanceSubTypes();

		NULL_POLY_TYPE = new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class) {

			@Override
			public String getCaption() {
				return "";
			}

			@Override
			public String toString() {
				return "PolymorphicControl.NULL_POLY_TYPE";
			}
		};

		setLayout(new BorderLayout());

		setBorder(BorderFactory.createTitledBorder(""));
		refreshUI();
	}

	@Override
	public void setPalceHolder(FieldControlPlaceHolder fieldControlPlaceHolder) {
		this.fieldControlPlaceHolder = fieldControlPlaceHolder;
		if (dynamicControl instanceof IAdvancedFieldControl) {
			((IAdvancedFieldControl) dynamicControl).setPalceHolder(fieldControlPlaceHolder);
		}
	}

	protected Component createTypeEnumerationControl() {
		List<ITypeInfo> possibleTypes = new ArrayList<ITypeInfo>(subTypes);
		{
			Object instance = data.getValue();
			if (instance != null) {
				ITypeInfo actualFieldValueType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
				if (!possibleTypes.contains(actualFieldValueType)) {
					instanceByEnumerationValueCache.put(actualFieldValueType, instance);
					possibleTypes.add(actualFieldValueType);
				}
			}
			if (data.isNullable()) {
				possibleTypes.add(0, NULL_POLY_TYPE);
			}
		}
		final ArrayAsEnumerationFactory enumFactory = ReflectionUIUtils
				.getPolymorphicTypesEnumerationfactory(swingRenderer.getReflectionUI(), polymorphicType, possibleTypes);
		ITypeInfo enumType = swingRenderer.getReflectionUI().getTypeInfo(enumFactory.getInstanceTypeInfoSource());
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				enumType);
		encapsulation.setTypeCaption(ReflectionUIUtils.composeMessage(polymorphicType.getCaption(), "Polymorphic Type"));
		encapsulation.setFieldNullable(false);
		encapsulation.setFieldCaption("");
		Object encapsulated = encapsulation.getInstance(new Accessor<Object>() {

			@Override
			public Object get() {
				Object instance = data.getValue();
				if (instance == null) {
					return enumFactory.getInstance(NULL_POLY_TYPE);
				} else {
					ITypeInfo actualFieldValueType = swingRenderer.getReflectionUI()
							.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
					instanceByEnumerationValueCache.put(actualFieldValueType, instance);
					return enumFactory.getInstance(actualFieldValueType);
				}
			}

			@Override
			public void set(Object value) {
				try {
					value = enumFactory.unwrapInstance(value);
					if (value == NULL_POLY_TYPE) {
						setDataValue(null);
					} else {
						ITypeInfo selectedPolyType = null;
						for (ITypeInfo subType : subTypes) {
							if (value.equals(subType)) {
								selectedPolyType = subType;
								break;
							}
						}
						Object instance = instanceByEnumerationValueCache.get(selectedPolyType);
						if (instance == null) {
							instance = swingRenderer.onTypeInstanciationRequest(PolymorphicControl.this,
									selectedPolyType, false);
							if (instance == null) {
								return;
							}
						}
						setDataValue(instance);
					}
				} finally {
					refreshDynamicControl();
				}
			}

			private void setDataValue(Object value) {
				updatingEnumeration = true;
				data.setValue(value);
				updatingEnumeration = false;
			}

		});
		JPanel result = swingRenderer.createForm(encapsulated);
		swingRenderer.getBusyIndicationDisabledByForm().put(result, true);
		return result;
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Component createDynamicControl(final ITypeInfo instanceType) {
		final EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				instanceType);
		encapsulation.setTypeCaption(ReflectionUIUtils.composeMessage(polymorphicType.getCaption(), "Polymorphic Value"));
		encapsulation.setFieldNullable(false);
		encapsulation.setFieldCaption("");
		final Object encapsulated = encapsulation.getInstance(new Accessor<Object>() {

			@Override
			public Object get() {
				return data.getValue();
			}

			@Override
			public void set(Object value) {
				data.setValue(value);
			}

		});
		EmbeddedFormControl result = new EmbeddedFormControl(swingRenderer, new ControlDataProxy(data) {

			@Override
			public Object getValue() {
				return encapsulated;
			}

			@Override
			public void setValue(Object value) {
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public ITypeInfo getType() {
				return swingRenderer.getReflectionUI().getTypeInfo(encapsulation.getInstanceTypeInfoSource());
			}

		});
		result.setPalceHolder(fieldControlPlaceHolder);
		return result;
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
				if (dynamicControl instanceof IAdvancedFieldControl) {
					if (((IAdvancedFieldControl) dynamicControl).refreshUI()) {
						return;
					}
				}
			}
			remove(dynamicControl);
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		lastInstanceType = instanceType;
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			remove(typeEnumerationControl);
		}
		if (!data.isGetOnly()) {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
		}
		validate();
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
	public boolean displayError(ReflectionUIError error) {
		return (dynamicControl instanceof IAdvancedFieldControl)
				&& ((IAdvancedFieldControl) dynamicControl).displayError(error);
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		if (updatingEnumeration) {
			return false;
		} else if (dynamicControl == null) {
			return false;
		} else if (dynamicControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) dynamicControl).handlesModificationStackUpdate();
		} else {
			return false;
		}
	}

	@Override
	public Object getFocusDetails() {
		boolean typeEnumerationControlFocused = SwingRendererUtils.hasOrContainsFocus(typeEnumerationControl);
		boolean dynamicControlFocused = false;
		Class<?> dynamicControlClass = null;
		Object dynamicControlFocusDetails = null;
		{
			if (dynamicControl != null) {
				dynamicControlFocused = SwingRendererUtils.hasOrContainsFocus(dynamicControl);
				if (dynamicControlFocused) {
					if (dynamicControl instanceof IAdvancedFieldControl) {
						dynamicControlFocusDetails = ((IAdvancedFieldControl) dynamicControl).getFocusDetails();
						dynamicControlClass = dynamicControl.getClass();
					}
				}
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("typeEnumerationControlFocused", typeEnumerationControlFocused);
		result.put("dynamicControlFocused", dynamicControlFocused);
		result.put("dynamicControlClass", dynamicControlClass);
		result.put("dynamicControlFocusDetails", dynamicControlFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		boolean typeEnumerationControlFocused = (Boolean) focusDetails.get("typeEnumerationControlFocused");
		boolean dynamicControlFocused = (Boolean) focusDetails.get("dynamicControlFocused");
		Class<?> dynamicControlClass = (Class<?>) focusDetails.get("dynamicControlClass");
		Object dynamicControlFocusDetails = focusDetails.get("dynamicControlFocusDetails");
		if (typeEnumerationControlFocused) {
			typeEnumerationControl.requestFocus();
		}
		if (dynamicControlFocused) {
			if (dynamicControl != null) {
				dynamicControl.requestFocus();
				if (dynamicControl instanceof IAdvancedFieldControl) {
					if (dynamicControl.getClass().equals(dynamicControlClass)) {
						((IAdvancedFieldControl) dynamicControl).requestDetailedFocus(dynamicControlFocusDetails);
					}
				}
			}
		}
	}

	@Override
	public void validateSubForm() throws Exception {
		if (dynamicControl instanceof IAdvancedFieldControl) {
			((IAdvancedFieldControl) dynamicControl).validateSubForm();
		}
	}

	@Override
	public ITypeInfo getDynamicObjectType() {
		return lastInstanceType;
	}

}
