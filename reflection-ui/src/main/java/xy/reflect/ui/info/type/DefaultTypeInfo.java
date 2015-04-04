package xy.reflect.ui.info.type;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DialogAccessControl;
import xy.reflect.ui.control.EmbeddedFormControl;
import xy.reflect.ui.control.NullableControl;
import xy.reflect.ui.control.PolymorphicEmbeddedForm;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorMethodInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultTypeInfo implements ITypeInfo {

	private static final String DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY = DefaultTypeInfo.class
			.getName() + "#IS_EMBEDDED_FORM_CONTENT_PROPRTY_KEY";
	protected Class<?> javaType;
	protected ReflectionUI reflectionUI;

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
		if (!isConcrete()) {
			return Collections.emptyList();
		}
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (Constructor<?> javaConstructor : javaType.getConstructors()) {
			result.add(new DefaultConstructorMethodInfo(reflectionUI, this,
					javaConstructor));
		}
		return result;
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
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (Field javaField : javaType.getFields()) {
			if (!PublicFieldInfo.isCompatibleWith(javaField)) {
				continue;
			}
			result.add(new PublicFieldInfo(reflectionUI, javaField));
		}
		for (Method javaMethod : javaType.getMethods()) {
			if (!GetterFieldInfo.isCompatibleWith(javaMethod, javaType)) {
				continue;
			}
			GetterFieldInfo getterFieldInfo = new GetterFieldInfo(reflectionUI,
					javaMethod, javaType);
			if (ReflectionUIUtils.findInfoByName(result,
					getterFieldInfo.getName()) != null) {
				continue;
			}
			result.add(getterFieldInfo);
		}
		sortFields(result);
		return result;
	}

	protected void sortFields(List<IFieldInfo> list) {
		Collections.sort(list, new Comparator<IFieldInfo>() {
			@Override
			public int compare(IFieldInfo f1, IFieldInfo f2) {
				int result;

				result = ReflectionUIUtils.compareNullables(f1.getCategory(),
						f2.getCategory());
				if (result != 0) {
					return result;
				}

				result = ReflectionUIUtils.compareNullables(f1.getType()
						.getName().toUpperCase(), f2.getType().getName()
						.toUpperCase());
				if (result != 0) {
					return result;
				}

				result = ReflectionUIUtils.compareNullables(f1.getName(),
						f2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	@Override
	public List<IMethodInfo> getMethods() {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (Method javaMethod : javaType.getMethods()) {
			if (!DefaultMethodInfo.isCompatibleWith(javaMethod, javaType)) {
				continue;
			}
			result.add(new DefaultMethodInfo(reflectionUI, javaMethod));
		}
		sortMethods(result);
		return result;
	}

	protected void sortMethods(List<IMethodInfo> list) {
		Collections.sort(list, new Comparator<IMethodInfo>() {
			@Override
			public int compare(IMethodInfo m1, IMethodInfo m2) {
				int result;

				result = ReflectionUIUtils.compareNullables(m1.getCategory(),
						m2.getCategory());
				if (result != 0) {
					return result;
				}

				String returnType1;
				{
					if (m1.getReturnValueType() == null) {
						returnType1 = "";
					} else {
						returnType1 = m1.getReturnValueType().getName();
					}
				}
				String returnType2;
				{
					if (m2.getReturnValueType() == null) {
						returnType2 = "";
					} else {
						returnType2 = m2.getReturnValueType().getName();
					}
				}
				result = ReflectionUIUtils.compareNullables(returnType1,
						returnType2);
				if (result != 0) {
					return result;
				}

				result = ReflectionUIUtils.compareNullables(m1.getName(),
						m2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	@Override
	public Component createFieldControl(Object object, IFieldInfo field) {
		if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return new PolymorphicEmbeddedForm(reflectionUI, object, field);
		} else {
			if (field.isNullable()) {
				return new NullableControl(reflectionUI, object, field, this);
			} else {
				return createNonNullFieldValueControl(object, field);
			}
		}
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
			field = preventNestedEmbeddedForm(field);
		} else {
			if (!fieldValueType.hasCustomFieldControl()) {
				if ((fieldValueType.getFields().size() + fieldValueType
						.getMethods().size() / 4) <= 5) {
					shouldCreateEmbeddedForm = true;
					field = preventNestedEmbeddedForm(field);
				}
			}
		}
		if (shouldCreateEmbeddedForm) {
			return new EmbeddedFormControl(reflectionUI, object, field);
		} else {
			return new DialogAccessControl(reflectionUI, object, field);
		}
	}

	protected IFieldInfo preventNestedEmbeddedForm(IFieldInfo field) {
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

		};
	}

	@Override
	public boolean supportsValue(Object value) {
		if (javaType.isPrimitive()) {
			return ReflectionUIUtils.primitiveToWrapperType(javaType)
					.isInstance(value);
		} else {
			return javaType.isInstance(value);
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
	public boolean isImmutable() {
		for (IFieldInfo field : getFields()) {
			if (!field.isReadOnly()) {
				return false;
			}
		}
		for (IMethodInfo method : getMethods()) {
			if (!method.isReadOnly()) {
				return false;
			}
		}
		return true;
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
			if (result.contains(objectClassName)) {
				String objectClassCaption = reflectionUI.getTypeInfo(
						reflectionUI.getTypeInfoSource(object)).getCaption();
				result = result.replace(objectClassName, objectClassCaption);
			}
			return result;
		}
	}

	@Override
	public String getDocumentation() {
		return ReflectionUIUtils.getAnnotatedInfoDocumentation(javaType);
	}

	@Override
	public void validate(Object object) throws Exception {
		for (Method method : ReflectionUIUtils
				.geAnnotatedtValidatingMethods(javaType)) {
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
