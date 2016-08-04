package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class PolymorphicEmbeddedForm extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;
	protected List<ITypeInfo> subTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected Component dynamicControl;
	protected EnumerationControl typeEnumerationControl;
	protected ITypeInfo polymorphicType;

	protected final ITypeInfo NULL_POLY_TYPE;
	protected ITypeInfo lastInstanceType;

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

	@Override
	public void requestFocus() {
		if (dynamicControl != null) {
			dynamicControl.requestFocus();
		} else {
			typeEnumerationControl.requestFocus();
		}
	}

	protected EnumerationControl createTypeEnumerationControl() {
		return new EnumerationControl(swingRenderer, object, new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

			@Override
			public Object getValue(Object object) {
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
			public void setValue(Object object, Object value) {
				try {
					if (value == NULL_POLY_TYPE) {
						field.setValue(object, null);
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
						field.setValue(object, instance);
					}
				} finally {
					refreshUI();
				}
			}

			@Override
			public boolean isGetOnly() {
				return false;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public ITypeInfo getType() {
				List<ITypeInfo> possibleValues = new ArrayList<ITypeInfo>(subTypes);
				if (PolymorphicEmbeddedForm.this.field.isNullable()) {
					possibleValues.add(0, NULL_POLY_TYPE);
				}
				return new ArrayAsEnumerationTypeInfo(swingRenderer.getReflectionUI(), possibleValues.toArray(),
						PolymorphicEmbeddedForm.class.getSimpleName() + " Enumeration Type");
			}

		});
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Component createDynamicControl(final ITypeInfo instanceType) {
		return swingRenderer.createFieldControl(object,
				new HiddenNullableFacetFieldInfoProxy(swingRenderer.getReflectionUI(), field) {

					@Override
					public ITypeInfo getType() {
						return instanceType;
					}

				});
	}

	protected void refreshDynamicControl(ITypeInfo instanceType) {
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
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			remove(typeEnumerationControl);
		}
		add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
		validate();
	}

	@Override
	public boolean refreshUI() {
		Object instance = field.getValue(object);
		ITypeInfo instanceType = null;
		if (instance != null) {
			instanceType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(instance));
		}
		refreshTypeEnumerationControl();
		refreshDynamicControl(instanceType);
		lastInstanceType = instanceType;
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

}
