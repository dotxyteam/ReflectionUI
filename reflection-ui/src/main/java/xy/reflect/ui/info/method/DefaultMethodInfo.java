package xy.reflect.ui.info.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultMethodInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaMethod;
	protected ITypeInfo returnValueType;
	protected List<IParameterInfo> parameters;

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
			if (returnValueType == null) {
				returnValueType = reflectionUI
						.getTypeInfo(new JavaTypeInfoSource(javaMethod
								.getReturnType(), javaMethod));
			}
			return returnValueType;
		}
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaMethod.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Parameter javaParameter = new Parameter(javaMethod, i);
				if (!DefaultParameterInfo.isCompatibleWith(javaParameter)) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI,
						javaParameter));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object[] args = new Object[javaMethod.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param);
		}
		try {
			return javaMethod.invoke(object, args);
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

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.SELF;
	}

	public static boolean isCompatibleWith(Method javaMethod,
			Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ReflectionUIUtils.isOverridenBy(commonMethod, javaMethod)) {
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
					if (javaMethod.equals(GetterFieldInfo.getValidSetterMethod(
							otherJavaMethod, containingJavaClass))) {
						return false;
					}
				}
			}
		}
		if (ReflectionUIUtils.isJavaClassMainMethod(javaMethod)) {
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
	public Runnable getUndoJob(Object object,
			InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData)
			throws Exception {
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
