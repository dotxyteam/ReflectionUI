package xy.reflect.ui.info.field;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
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

	public GetterFieldInfo(ReflectionUI reflectionUI, Method javaGetterMethod, Class<?> containingJavaClass) {
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
		Method javaSetterMethod = GetterFieldInfo.getValidSetterMethod(javaGetterMethod, containingJavaClass);
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
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo setter = getSetterMethodInfo();
		setter.invoke(object, new InvocationData(value));
	}

	@Override
	public boolean isNullable() {
		return getType().isPassedByReference();
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return getSetterMethodInfo() == null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.PROXY;
	}

	

	@Override
	public String toString() {
		return "GetterFieldInfo [javaGetterMethod=" + javaGetterMethod + "]";
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
		return javaGetterMethod.equals(((GetterFieldInfo) obj).javaGetterMethod);
	}

	public static String getFieldName(String getterMethodName) {
		Matcher m = Pattern.compile("^(?:get|is|has)([A-Z].*)").matcher(getterMethodName);
		if (!m.matches()) {
			return null;
		}
		String result = m.group(1);
		if (result != null) {
			result = ReflectionUIUtils.changeCase(result, false, 0, 1);
		}
		return result;
	}

	public static Method getValidSetterMethod(Method javaGetterMethod, Class<?> containingJavaClass) {
		String fieldName = getFieldName(javaGetterMethod.getName());
		String setterMethodName = "set" + ReflectionUIUtils.changeCase(fieldName, true, 0, 1);
		Method result;
		try {
			result = containingJavaClass.getMethod(setterMethodName, new Class[] { javaGetterMethod.getReturnType() });
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		}
		if (result.getExceptionTypes().length > 0) {
			return null;
		}
		return result;
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		if (GetterFieldInfo.getFieldName(javaMethod.getName()) == null) {
			return false;
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
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
