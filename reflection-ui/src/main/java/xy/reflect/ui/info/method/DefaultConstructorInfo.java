package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;

public class DefaultConstructorInfo extends AbstractConstructorInfo {

	protected Constructor<?> javaConstructor;
	protected ReflectionUI reflectionUI;
	protected ArrayList<IParameterInfo> parameters;

	public DefaultConstructorInfo(ReflectionUI reflectionUI, Constructor<?> javaConstructor) {
		this.reflectionUI = reflectionUI;
		this.javaConstructor = javaConstructor;
		resolveJavaReflectionModelAccessProblems();
	}

	public static boolean isCompatibleWith(Constructor<?> constructor) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		if (declaringClass.getEnclosingClass() != null) {
			if (!Modifier.isStatic(declaringClass.getModifiers())) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaConstructor.setAccessible(true);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaConstructor.getDeclaringClass()));
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public IInfoProxyFactory getReturnValueTypeSpecificities() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaConstructor.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!DefaultParameterInfo.isCompatibleWith(new Parameter(javaConstructor, i))) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI, new Parameter(javaConstructor, i)));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object[] args = new Object[javaConstructor.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param);
		}
		try {
			return javaConstructor.newInstance(args);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		} catch (InstantiationException e) {
			throw new ReflectionUIError(e);
		}
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
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public int hashCode() {
		return javaConstructor.hashCode();
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
		if (!javaConstructor.equals(((DefaultConstructorInfo) obj).javaConstructor)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultConstructorInfo [javaConstructor=" + javaConstructor + "]";
	}

}
