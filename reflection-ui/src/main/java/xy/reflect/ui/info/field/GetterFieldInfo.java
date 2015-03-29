package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IBooleanTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class GetterFieldInfo implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaGetterMethod;
	private Class<?> containingJavaClass;

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

	protected IMethodInfo getGetterMethodInfo() {
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
		return getGetterMethodInfo().getReturnValueType();
	}

	@Override
	public String getCaption() {
		String result = ReflectionUIUtils.identifierToCaption(getName());
		if (getType() instanceof IBooleanTypeInfo) {
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
		return getGetterMethodInfo().invoke(object,
				Collections.<String, Object> emptyMap());
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo setter = getSetterMethodInfo();
		String paramName = setter.getParameters().get(0).getName();
		setter.invoke(object,
				Collections.<String, Object> singletonMap(paramName, value));
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

	public static String getFieldName(String methodName) {
		Matcher m = Pattern.compile("^(?:get|is|has)([A-Z].*)").matcher(
				methodName);
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
		if (GetterFieldInfo.getFieldName(javaMethod.getName()) == null) {
			return false;
		}
		if (Modifier.isStatic(javaMethod.getModifiers())) {
			return false;
		}
		if (javaMethod.getParameterTypes().length > 0) {
			return false;
		}
		for (Method defaultMethod : Object.class.getMethods()) {
			if (ReflectionUIUtils.getJavaMethodSignature(defaultMethod).equals(
					ReflectionUIUtils.getJavaMethodSignature(javaMethod))) {
				return false;
			}
		}
		if (javaMethod.getExceptionTypes().length > 0) {
			return false;
		}
		if(ReflectionUIUtils.geAnnotatedtValidatingMethods(containingJavaClass).contains(javaMethod)){
			return false;
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return ReflectionUIUtils.getAnnotatedInfoCategory(javaGetterMethod);
	}

	@Override
	public String getDocumentation() {
		for(Field field: ReflectionUIUtils.getALlFields(containingJavaClass)){
			if(field.getName().equals(getName())){
				String result = ReflectionUIUtils
						.getAnnotatedInfoDocumentation(field);
				if(result != null){
					return result;
				}
			}
		}		
		String result = ReflectionUIUtils
				.getAnnotatedInfoDocumentation(javaGetterMethod);
		if (result == null) {
			return null;
		}
		Method setter = getSetterMethod(javaGetterMethod, containingJavaClass);
		if (setter != null) {
			String setterDoc = ReflectionUIUtils
					.getAnnotatedInfoDocumentation(setter);
			if (setterDoc != null) {
				result += "\nand\n" + setterDoc;
			}
		}
		return result;
	}

}
