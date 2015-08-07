package xy.reflect.ui.info.type;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DialogAccessControl;
import xy.reflect.ui.control.EmbeddedFormControl;
import xy.reflect.ui.control.EnumerationControl;
import xy.reflect.ui.control.NullableControl;
import xy.reflect.ui.control.PolymorphicEmbeddedForm;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorMethodInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultTypeInfo implements ITypeInfo {

	protected static final String DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY = DefaultTypeInfo.class
			.getName() + "#IS_EMBEDDED_FORM_CONTENT_PROPRTY_KEY";
	protected Class<?> javaType;
	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> fields;
	protected List<IMethodInfo> methods;
	protected List<IMethodInfo> constructors;

	public DefaultTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		if (javaType == null) {
			throw new ReflectionUIError();
		}
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	@Override
	public boolean isConcrete() {
		if ((!javaType.isPrimitive())
				&& Modifier.isAbstract(javaType.getModifiers())) {
			return false;
		}
		if (javaType == Object.class) {
			return false;
		}
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (constructors == null) {
			constructors = new ArrayList<IMethodInfo>();
			if (!isConcrete()) {
				return Collections.emptyList();
			}
			for (Constructor<?> javaConstructor : javaType.getConstructors()) {
				if(!DefaultConstructorMethodInfo.isCompatibleWith(javaConstructor)){
					continue;
				}
				constructors.add(new DefaultConstructorMethodInfo(reflectionUI,
						this, javaConstructor));
			}
		}
		return constructors;
	}

	@Override
	public String getName() {
		return javaType.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(javaType.getSimpleName());
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public List<IFieldInfo> getFields() {
		if (fields == null) {
			fields = new ArrayList<IFieldInfo>();
			for (Field javaField : javaType.getFields()) {
				if (!PublicFieldInfo.isCompatibleWith(javaField)) {
					continue;
				}
				fields.add(new PublicFieldInfo(reflectionUI, javaField, javaType));
			}
			for (Method javaMethod : javaType.getMethods()) {
				if (!GetterFieldInfo.isCompatibleWith(javaMethod, javaType)) {
					continue;
				}
				GetterFieldInfo getterFieldInfo = new GetterFieldInfo(
						reflectionUI, javaMethod, javaType);
				if (ReflectionUIUtils.findInfoByName(fields,
						getterFieldInfo.getName()) != null) {
					continue;
				}
				fields.add(getterFieldInfo);
			}
			ReflectionUIUtils.sortFields(fields);
		}
		return fields;
	}

	@Override
	public List<IMethodInfo> getMethods() {
		if (methods == null) {
			methods = new ArrayList<IMethodInfo>();
			for (Method javaMethod : javaType.getMethods()) {
				if (!DefaultMethodInfo.isCompatibleWith(javaMethod, javaType)) {
					continue;
				}
				methods.add(new DefaultMethodInfo(reflectionUI, javaMethod));
			}
			ReflectionUIUtils.sortMethods(methods);
		}
		return methods;
	}

	@Override
	public Component createFieldControl(Object object, IFieldInfo field) {
		if (field.getValueOptions(object) != null) {
			return createOptionsControl(object, field);
		} else if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return new PolymorphicEmbeddedForm(reflectionUI, object, field);
		} else if (field.isNullable()) {
			return new NullableControl(reflectionUI, object, field, this);
		} else {
			return createNonNullFieldValueControl(object, field);
		}
	}

	protected Component createOptionsControl(final Object object, final IFieldInfo field) {
		return new EnumerationControl(reflectionUI, object, new FieldInfoProxy(field){

			@Override
			public ITypeInfo getType() {
				final ITypeInfo baseType = field.getType();
				return new IEnumerationTypeInfo() {
					
					@Override
					public Map<String, Object> getSpecificProperties() {
						return Collections.emptyMap();
					}
					
					@Override
					public String getName() {
						return "";
					}
					
					@Override
					public String getOnlineHelp() {
						return null;
					}
					
					@Override
					public String getCaption() {
						return "";
					}
					
					@Override
					public void validate(Object object) throws Exception {
						baseType.validate(object);
					}
					
					@Override
					public String toString(Object object) {
						return baseType.toString(object);
					}
					
					@Override
					public boolean supportsInstance(Object object) {
						return baseType.supportsInstance(object);
					}
					
					@Override
					public boolean isConcrete() {
						return baseType.isConcrete();
					}
					
					@Override
					public boolean hasCustomFieldControl() {
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
					public Component createFieldControl(Object object, IFieldInfo field) {
						throw new ReflectionUIError();
					}
					
					@Override
					public Object[] getPossibleValues() {
						return field.getValueOptions(object);
					}
					
					@Override
					public String formatEnumerationItem(Object object) {
						return baseType.toString(object);
					}
				};
			}
			
		});
	}

	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		Object fieldValue = field.getValue(object);
		final ITypeInfo fieldValueType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(fieldValue));
		if (!fieldValueType.equals(this)) {
			if (fieldValueType instanceof DefaultTypeInfo) {
				return ((DefaultTypeInfo) fieldValueType)
						.createNonNullFieldValueControl(object,
								new FieldInfoProxy(field) {
									@Override
									public ITypeInfo getType() {
										return fieldValueType;
									}
								});
			}
		}
		boolean shouldCreateEmbeddedForm = false;
		if (Boolean.TRUE.equals(field.getSpecificProperties().get(
				DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY))) {
			field = preventRecursiveEmbeddedForm(field);
		} else {
			if (!fieldValueType.hasCustomFieldControl()) {
				if ((fieldValueType.getFields().size() + fieldValueType
						.getMethods().size() / 4) <= 5) {
					shouldCreateEmbeddedForm = true;
					field = preventRecursiveEmbeddedForm(field);
				}
			}
		}
		if (shouldCreateEmbeddedForm) {
			return new EmbeddedFormControl(reflectionUI, object, field);
		} else {
			return new DialogAccessControl(reflectionUI, object, field);
		}
	}

	protected IFieldInfo preventRecursiveEmbeddedForm(IFieldInfo field) {
		return new FieldInfoProxy(field) {

			@Override
			public Object getValue(Object object) {
				Object result = super.getValue(object);
				if (result != null) {
					ITypeInfo resultType = reflectionUI
							.getTypeInfo(reflectionUI.getTypeInfoSource(result));
					resultType = new TypeInfoProxyConfiguration() {

						@Override
						protected List<IFieldInfo> getFields(ITypeInfo type) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>();
							for (IFieldInfo field : super.getFields(type)) {
								field = new FieldInfoProxy(field) {
									@Override
									public Map<String, Object> getSpecificProperties() {
										Map<String, Object> result = new HashMap<String, Object>(
												super.getSpecificProperties());
										result.put(
												DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY,
												true);
										return result;
									}
								};
								result.add(field);
							}
							return result;
						}

					}.get(resultType);
					result = new PrecomputedTypeInfoInstanceWrapper(result,
							resultType);
				}
				return result;
			}

			@Override
			public void setValue(Object object, Object value) {
				if (value != null) {
					value = ((PrecomputedTypeInfoInstanceWrapper) value)
							.getInstance();
				}
				super.setValue(object, value);
			}

		};
	}

	@Override
	public boolean supportsInstance(Object object) {
		if (javaType.isPrimitive()) {
			return ReflectionUIUtils.primitiveToWrapperType(javaType)
					.isInstance(object);
		} else {
			return javaType.isInstance(object);
		}
	}

	@Override
	public int hashCode() {
		return javaType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!javaType.equals(((DefaultTypeInfo) obj).javaType)) {
			return false;
		}
		return true;
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return null;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return false;
	}

	@Override
	public String toString(Object object) {
		if (object == null) {
			return null;
		} else {
			String result = object.toString();
			String objectClassName = object.getClass().getName();
			String objectClassCaption = reflectionUI.getTypeInfo(
					reflectionUI.getTypeInfoSource(object)).getCaption();
			result = result.replaceAll(objectClassName.replace(".", "\\.")
					.replace("$", "\\$") + "@([0-9a-z]+)", objectClassCaption
					+ " $1");
			result = result.replace(objectClassName, objectClassCaption);
			return result;
		}
	}

	@Override
	public String getOnlineHelp() {
		return ReflectionUIUtils.getAnnotatedInfoOnlineHelp(javaType);
	}

	@Override
	public void validate(Object object) throws Exception {
		for (Method method : ReflectionUIUtils.getAnnotatedValidatingMethods(javaType)) {
			try {
				method.invoke(object);
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e.getCause());
			}
		}
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
