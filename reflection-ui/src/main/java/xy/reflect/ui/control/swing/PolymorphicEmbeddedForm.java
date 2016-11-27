package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationTypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class PolymorphicEmbeddedForm extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;
	protected List<ITypeInfo> subTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected Component dynamicControl;
	protected Component typeEnumerationControl;
	protected ITypeInfo polymorphicType;

	protected final ITypeInfo NULL_POLY_TYPE;
	protected ITypeInfo lastInstanceType;
	protected boolean updatingEnumeration = false;

	public PolymorphicEmbeddedForm(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.field = field;
		this.polymorphicType = field.getType();
		this.subTypes = polymorphicType.getPolymorphicInstanceSubTypes();

		NULL_POLY_TYPE = new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class) {

			@Override
			public String getCaption() {
				return "";
			}

		};

		setLayout(new BorderLayout());

		setBorder(BorderFactory.createTitledBorder(""));
		refreshUI();
	}

	protected Component createTypeEnumerationControl() {
		List<ITypeInfo> possibleValues = new ArrayList<ITypeInfo>(subTypes);
		if (PolymorphicEmbeddedForm.this.field.isNullable()) {
			possibleValues.add(0, NULL_POLY_TYPE);
		}
		ITypeInfo fieldType = new ArrayAsEnumerationTypeInfo(swingRenderer.getReflectionUI(), possibleValues.toArray(),
				PolymorphicEmbeddedForm.class.getSimpleName() + " Enumeration Type");
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				fieldType);
		encapsulation.setFieldNullable(false);
		Object encapsulated = encapsulation.getInstance(new Accessor<Object>(){

			@Override
			public Object get() {
				Object instance = field.getValue(object);
				if (instance == null) {
					return NULL_POLY_TYPE;
				} else {
					ITypeInfo actualFieldValueType = swingRenderer.getReflectionUI()
							.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
					instanceByEnumerationValueCache.put(actualFieldValueType, instance);
					return actualFieldValueType;
				}
			}

			@Override
			public void set(Object value) {
				try {
					if (value == NULL_POLY_TYPE) {
						setFieldValue(object, null);
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
							instance = swingRenderer.onTypeInstanciationRequest(PolymorphicEmbeddedForm.this,
									selectedPolyType, false);
							if (instance == null) {
								return;
							}
						}
						setFieldValue(object, instance);
					}
				} finally {
					refreshUI();
				}
			}

			private void setFieldValue(Object object, Object value) {
				updatingEnumeration = true;
				field.setValue(object, value);
				updatingEnumeration = false;
			}
			
			
		});
		return swingRenderer.createObjectForm(encapsulated);
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Component createDynamicControl(final ITypeInfo instanceType) {
		IFieldInfo fiedlProxy = new HiddenNullableFacetFieldInfoProxy(swingRenderer.getReflectionUI(), field) {

			@Override
			public ITypeInfo getType() {
				return instanceType;
			}

		};
		return swingRenderer.createFieldControl(object, fiedlProxy);
	}

	protected void refreshDynamicControl() {
		Object instance = field.getValue(object);
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
			swingRenderer.handleComponentSizeChange(this);
		} else if ((lastInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			swingRenderer.handleComponentSizeChange(this);
		} else {
			if (lastInstanceType.equals(instanceType)) {
				if (dynamicControl instanceof IFieldControl) {
					if (((IFieldControl) dynamicControl).refreshUI()) {
						return;
					}
				}
			}
			remove(dynamicControl);
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			swingRenderer.handleComponentSizeChange(this);
		}
		lastInstanceType = instanceType;
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			remove(typeEnumerationControl);
		}
		if (!field.isGetOnly()) {
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
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return (dynamicControl instanceof IFieldControl) && ((IFieldControl) dynamicControl).displayError(error);
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		if (updatingEnumeration) {
			return false;
		} else if (dynamicControl == null) {
			return false;
		} else if (dynamicControl instanceof IFieldControl) {
			return ((IFieldControl) dynamicControl).handlesModificationStackUpdate();
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
					if (dynamicControl instanceof IFieldControl) {
						dynamicControlFocusDetails = ((IFieldControl) dynamicControl).getFocusDetails();
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
				if (dynamicControl instanceof IFieldControl) {
					if (dynamicControl.getClass().equals(dynamicControlClass)) {
						((IFieldControl) dynamicControl).requestDetailedFocus(dynamicControlFocusDetails);
					}
				}
			}
		}
	}

}
