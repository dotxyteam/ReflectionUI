package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class GetterFieldInfo implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaGetterMethod;
	protected Class<?> containingJavaClass;
	protected ITypeInfo type;

	public GetterFieldInfo(ReflectionUI reflectionUI, Method javaGetterMethod,
			Class<?> containingJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaGetterMethod = javaGetterMethod;
		this.containingJavaClass = containingJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaGetterMethod.setAccessible(true);
	}

	public IMethodInfo getGetterMethodInfo() {
		return new DefaultMethodInfo(reflectionUI, javaGetterMethod);
	}

	protected IMethodInfo getSetterMethodInfo() {
		Method javaSetterMethod = GetterFieldInfo.getSetterMethod(
				javaGetterMethod, containingJavaClass);
		if (javaSetterMethod == null) {
			return null;
		}
		return new DefaultMethodInfo(reflectionUI, javaSetterMethod);
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = getGetterMethodInfo().getReturnValueType();
		}
		return type;
	}

	@Override
	public String getCaption() {
		String result = ReflectionUIUtils.identifierToCaption(getName());
		if (BooleanTypeInfo.isCompatibleWith(javaGetterMethod.getReturnType())) {
			if (javaGetterMethod.getName().matches("^is[A-Z].*")) {
				result = "Is " + result;
			} else if (javaGetterMethod.getName().matches("^has[A-Z].*")) {
				result = "Has " + result;
			}
		}
		return result;
	}

	@Override
	public Object getValue(Object object) {
		return getGetterMethodInfo().invoke(object, new InvocationData());
	}

	@Override
	public Object[] getValueOptions(Object object) {
		String fieldName = getFieldName(javaGetterMethod.getName());
		if (fieldName == null) {
			return null;
		}
		return ReflectionUIUtils.getFieldValueOptionsFromAnnotatedMember(
				object, containingJavaClass, fieldName, reflectionUI);
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo setter = getSetterMethodInfo();
		setter.invoke(object, new InvocationData(value));
	}

	@Override
	public boolean isNullable() {
		return !javaGetterMethod.getReturnType().isPrimitive();
	}

	@Override
	public boolean isReadOnly() {
		return getSetterMethodInfo() == null;
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		return GetterFieldInfo.getFieldName(javaGetterMethod.getName());
	}

	@Override
	public int hashCode() {
		return javaGetterMethod.hashCode();
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
		return javaGetterMethod
				.equals(((GetterFieldInfo) obj).javaGetterMethod);
	}

	public static String getFieldName(String getterMethodName) {
		Matcher m = Pattern.compile("^(?:get|is|has)([A-Z].*)").matcher(
				getterMethodName);
		if (!m.matches()) {
			return null;
		}
		String result = m.group(1);
		if (result != null) {
			result = ReflectionUIUtils.changeCase(result, false, 0, 1);
		}
		return result;
	}

	public static Method getSetterMethod(Method javaGetterMethod,
			Class<?> containingJavaClass) {
		String fieldName = getFieldName(javaGetterMethod.getName());
		String setterMethodName = "set"
				+ ReflectionUIUtils.changeCase(fieldName, true, 0, 1);
		Method result;
		try {
			result = containingJavaClass.getMethod(setterMethodName,
					new Class[] { javaGetterMethod.getReturnType() });
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		}
		if (!ReflectionUIUtils.equalsOrBothNull(
				ReflectionUIUtils.getAnnotatedInfoCategory(javaGetterMethod),
				ReflectionUIUtils.getAnnotatedInfoCategory(result))) {
			return null;
		}
		return result;
	}

	public static boolean isCompatibleWith(Method javaMethod,
			Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		if (GetterFieldInfo.getFieldName(javaMethod.getName()) == null) {
			return false;
		}
		if (Modifier.isStatic(javaMethod.getModifiers())) {
			if (GetterFieldInfo
					.getSetterMethod(javaMethod, containingJavaClass) == null) {
				return false;
			}
		}
		if (javaMethod.getParameterTypes().length > 0) {
			return false;
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ReflectionUIUtils.isOverridenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		if (javaMethod.getExceptionTypes().length > 0) {
			return false;
		}
		if (ReflectionUIUtils
				.getAnnotatedValidatingMethods(containingJavaClass).contains(
						javaMethod)) {
			return false;
		}
		if (javaMethod.getAnnotation(ValueOptionsForField.class) != null) {
			return false;
		}
		if (ReflectionUIUtils.isInfoHidden(javaMethod)) {
			return false;
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return ReflectionUIUtils.getAnnotatedInfoCategory(javaGetterMethod);
	}

	@Override
	public String getOnlineHelp() {
		for (Field field : ReflectionUIUtils.getALlFields(containingJavaClass)) {
			if (field.getName().equals(getName())) {
				String result = ReflectionUIUtils
						.getAnnotatedInfoOnlineHelp(field);
				if (result != null) {
					return result;
				}
			}
		}
		String result = ReflectionUIUtils
				.getAnnotatedInfoOnlineHelp(javaGetterMethod);
		if (result == null) {
			return null;
		}
		Method setter = getSetterMethod(javaGetterMethod, containingJavaClass);
		if (setter != null) {
			String setterDoc = ReflectionUIUtils
					.getAnnotatedInfoOnlineHelp(setter);
			if (setterDoc != null) {
				result += "\nand\n" + setterDoc;
			}
		}
		return result;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
