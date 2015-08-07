package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class PolymorphicEmbeddedForm extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected List<ITypeInfo> subTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected Component dynamicControl;
	protected EnumerationControl typeEnumerationControl;
	protected ITypeInfo polymorphicType;

	protected final ITypeInfo NULL_POLY_TYPE = new DefaultTypeInfo(
			reflectionUI, Object.class) {

		@Override
		public String getCaption() {
			return "";
		}

	};
	protected ITypeInfo lastInstanceType;

	public PolymorphicEmbeddedForm(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.polymorphicType = field.getType();
		this.subTypes = polymorphicType.getPolymorphicInstanceSubTypes();

		setLayout(new BorderLayout());

		setBorder(BorderFactory.createTitledBorder(""));
		refreshUI();
	}

	protected EnumerationControl createTypeEnumerationControl() {
		return new EnumerationControl(reflectionUI, object, new IFieldInfo() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getCaption() {
				return null;
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}

			@Override
			public Object getValue(Object object) {
				Object instance = field.getValue(object);
				if (instance == null) {
					return NULL_POLY_TYPE;
				} else {
					ITypeInfo actualFieldValueType = reflectionUI
							.getTypeInfo(reflectionUI
									.getTypeInfoSource(instance));
					instanceByEnumerationValueCache.put(
							actualFieldValueType, instance);
					return actualFieldValueType;
				}
			}

			@Override
			public Object[] getValueOptions(Object object) {
				return null;
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
						Object instance = instanceByEnumerationValueCache
								.get(selectedPolyType);
						if (instance == null) {
							instance = reflectionUI.onTypeInstanciationRequest(
									PolymorphicEmbeddedForm.this,
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
			public boolean isReadOnly() {
				return false;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public ITypeInfo getType() {
				return new IEnumerationTypeInfo() {

					@Override
					public String getName() {
						return "";
					}

					@Override
					public String getCaption() {
						return "";
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						return Collections.emptyMap();
					}

					@Override
					public boolean supportsInstance(Object object) {
						return object instanceof ITypeInfo;
					}

					@Override
					public boolean isConcrete() {
						return true;
					}

					@Override
					public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
						return null;
					}

					@Override
					public List<IMethodInfo> getMethods() {
						return Collections.emptyList();
					}

					@Override
					public List<IFieldInfo> getFields() {
						return Collections.emptyList();
					}

					@Override
					public List<IMethodInfo> getConstructors() {
						return Collections.emptyList();
					}

					@Override
					public Component createFieldControl(Object object,
							IFieldInfo field) {
						throw new ReflectionUIError();
					}

					@Override
					public Object[] getPossibleValues() {
						List<ITypeInfo> result = new ArrayList<ITypeInfo>();
						if (PolymorphicEmbeddedForm.this.field.isNullable()) {
							result.add(NULL_POLY_TYPE);
						}
						for (ITypeInfo subType : subTypes) {
							result.add(subType);
						}
						return result.toArray();
					}

					@Override
					public boolean hasCustomFieldControl() {
						return true;
					}

					@Override
					public String toString(Object object) {
						return object.toString();
					}

					@Override
					public String getOnlineHelp() {
						return null;
					}

					@Override
					public String formatEnumerationItem(Object object) {
						return ((ITypeInfo) object).getCaption();
					}

					@Override
					public void validate(Object object) throws Exception {
					}

				};
			}

			@Override
			public String getOnlineHelp() {
				return null;
			}
		});
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Component createDynamicControl(final ITypeInfo instanceType) {
		return instanceType.createFieldControl(object,
				new HiddenNullableFacetFieldInfoProxy(reflectionUI, field) {

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
			reflectionUI.handleComponentSizeChange(this);
		} else if ((lastInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			reflectionUI.handleComponentSizeChange(this);
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
			reflectionUI.handleComponentSizeChange(this);
		}
	}

	protected void refreshTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			remove(typeEnumerationControl);
		}
		add(typeEnumerationControl = createTypeEnumerationControl(),
				BorderLayout.NORTH);
		validate();
	}

	@Override
	public boolean refreshUI() {
		Object instance = field.getValue(object);
		ITypeInfo instanceType = null;
		if (instance != null) {
			instanceType = reflectionUI.getTypeInfo(reflectionUI
					.getTypeInfoSource(instance));
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
		return (dynamicControl instanceof IFieldControl)
				&& ((IFieldControl) dynamicControl).displayError(error);
	}

}
