


package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Constructor information extracted from the given Java constructor.
 * 
 * @author olitank
 *
 */
public class DefaultConstructorInfo extends AbstractConstructorInfo {

	protected Constructor<?> javaConstructor;
	protected ReflectionUI reflectionUI;
	protected ArrayList<IParameterInfo> parameters;
	protected ITypeInfo returnValueType;

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
		if (returnValueType == null) {
			returnValueType = reflectionUI
					.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, javaConstructor.getDeclaringClass(), null));
		}
		return returnValueType;
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
	public Object invoke(Object ignore, InvocationData invocationData) {
		Object[] args = new Object[javaConstructor.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param.getPosition());
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((javaConstructor == null) ? 0 : javaConstructor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultConstructorInfo other = (DefaultConstructorInfo) obj;
		if (javaConstructor == null) {
			if (other.javaConstructor != null)
				return false;
		} else if (!javaConstructor.equals(other.javaConstructor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultConstructorInfo [javaConstructor=" + javaConstructor + "]";
	}

}
