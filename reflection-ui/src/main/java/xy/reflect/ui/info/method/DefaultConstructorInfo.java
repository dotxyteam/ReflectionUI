
package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Constructor information extracted from the given Java constructor.
 * 
 * @author olitank
 *
 */
public class DefaultConstructorInfo extends AbstractConstructorInfo {

	protected Constructor<?> javaConstructor;
	protected Class<?> objectJavaClass;
	protected ReflectionUI reflectionUI;
	protected ArrayList<IParameterInfo> parameters;

	public DefaultConstructorInfo(ReflectionUI reflectionUI, Constructor<?> javaConstructor, Class<?> objectJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaConstructor = javaConstructor;
		this.objectJavaClass = objectJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	public Constructor<?> getJavaConstructor() {
		return javaConstructor;
	}

	public static boolean isCompatibleWith(Constructor<?> constructor, Class<?> objectJavaClass) {
		if (objectJavaClass.getEnclosingClass() != null) {
			if (!Modifier.isStatic(objectJavaClass.getModifiers())) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		try {
			javaConstructor.setAccessible(true);
		} catch (Throwable t) {
			reflectionUI.logDebug(t);
		}
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(objectJavaClass, null));
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Parameter[] javaParameters = javaConstructor.getParameters();
			for (int i = 0; i < javaParameters.length; i++) {
				if (!DefaultParameterInfo.isCompatibleWith(javaParameters[i])) {
					continue;
				}
				parameters.add(
						new DefaultParameterInfo(reflectionUI, javaParameters[i], i, javaConstructor, objectJavaClass));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object ignore, InvocationData invocationData) {
		Object[] args = new Object[javaConstructor.getParameterTypes().length];
		for (int i = 0; i < args.length; i++) {
			args[i] = invocationData.getParameterValue(i);
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
		result = prime * result + ((objectJavaClass == null) ? 0 : objectJavaClass.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		if (objectJavaClass == null) {
			if (other.objectJavaClass != null)
				return false;
		} else if (!objectJavaClass.equals(other.objectJavaClass))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultConstructorInfo [javaConstructor=" + javaConstructor + ", objectJavaClass=" + objectJavaClass
				+ "]";
	}

}
