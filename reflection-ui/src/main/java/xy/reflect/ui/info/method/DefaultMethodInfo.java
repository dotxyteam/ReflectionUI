package xy.reflect.ui.info.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ModificationStack.IModification;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultMethodInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaMethod;

	public DefaultMethodInfo(ReflectionUI reflectionUI, Method javaMethod) {
		this.reflectionUI = reflectionUI;
		this.javaMethod = javaMethod;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaMethod.setAccessible(true);
	}

	@Override
	public String getCaption() {
		String result = ReflectionUIUtils.identifierToCaption(javaMethod
				.getName());
		result = result.replaceAll("^Get ", "Show ");
		return result;
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (javaMethod.getReturnType() == void.class) {
			return null;
		} else {
			return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaMethod
					.getReturnType(), javaMethod));
		}
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		Class<?>[] parameterTypes = javaMethod.getParameterTypes();
		Annotation[][] parameterAnnotations = javaMethod.getParameterAnnotations();
		for (int i = 0; i<parameterTypes.length; i++) {
			Class<?> paramType = parameterTypes[i];
			Annotation[] paramAnnotations = parameterAnnotations[i];
			result.add(new DefaultParameterInfo(reflectionUI, javaMethod,
					paramType, paramAnnotations, i));
			i++;
		}
		return result;
	}

	@Override
	public Object invoke(Object object, Map<String, Object> valueByParameterName) {
		List<Object> args = new ArrayList<Object>();
		for (IParameterInfo param : getParameters()) {
			if (valueByParameterName.containsKey(param.getName())) {
				args.add(valueByParameterName.get(param.getName()));
			} else {
				args.add(param.getDefaultValue());
			}
		}
		try {
			return javaMethod.invoke(object, args.toArray());
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getCaption());
		if (getParameters().size() > 0) {
			result.append(" - specify ");
			result.append(ReflectionUIUtils
					.formatParameterList(getParameters()));
		}
		return result.toString();
	}

	@Override
	public String getName() {
		return javaMethod.getName();
	}

	@Override
	public int hashCode() {
		return javaMethod.hashCode();
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
		if (!javaMethod.equals(((DefaultMethodInfo) obj).javaMethod)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return Modifier.isStatic(javaMethod.getModifiers());
	}

	public static boolean isCompatibleWith(Method javaMethod,
			Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		for (Method commonMethod : Object.class.getDeclaredMethods()) {
			if (ReflectionUIUtils.getJavaMethodSignature(commonMethod).equals(
					ReflectionUIUtils.getJavaMethodSignature(javaMethod))) {
				return false;
			}
		}
		if (GetterFieldInfo.isCompatibleWith(javaMethod, containingJavaClass)) {
			return false;
		}
		for (Method otherJavaMethod : containingJavaClass.getMethods()) {
			if (!otherJavaMethod.equals(javaMethod)) {
				if (GetterFieldInfo.isCompatibleWith(otherJavaMethod,
						containingJavaClass)) {
					if (javaMethod.equals(GetterFieldInfo.getSetterMethod(
							otherJavaMethod, containingJavaClass))) {
						return false;
					}
				}
			}
		}
		if(ReflectionUIUtils.geAnnotatedtValidatingMethods(containingJavaClass).contains(javaMethod)){
			return false;
		}		
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return ReflectionUIUtils
						.getAnnotatedInfoCategory(javaMethod);
	}

	@Override
	public String getDocumentation() {
		return ReflectionUIUtils.getAnnotatedInfoDocumentation(javaMethod);
	}

	@Override
	public IModification getUndoModification(Object object, Map<String, Object> valueByParameterName) {
		return null;
	}

	@Override
	public void validateParameters(Object object,
			Map<String, Object> valueByParameterName) throws Exception {
	}

}
