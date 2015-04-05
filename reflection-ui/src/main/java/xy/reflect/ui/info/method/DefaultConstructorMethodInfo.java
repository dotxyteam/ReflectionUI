package xy.reflect.ui.info.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultConstructorMethodInfo extends AbstractConstructorMethodInfo {

	protected Constructor<?> javaConstructor;
	protected ReflectionUI reflectionUI;

	public DefaultConstructorMethodInfo(ReflectionUI reflectionUI,
			ITypeInfo ownerType, Constructor<?> javaConstructor) {
		super(ownerType);
		this.reflectionUI = reflectionUI;
		this.javaConstructor = javaConstructor;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaConstructor.setAccessible(true);
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		Class<?>[] parameterTypes = javaConstructor.getParameterTypes();
		Annotation[][] parameterAnnotations = javaConstructor.getParameterAnnotations();
		for (int i = 0; i<parameterTypes.length; i++) {
			Class<?> paramType = parameterTypes[i];
			Annotation[] paramAnnotations = parameterAnnotations[i];
			result.add(new DefaultParameterInfo(reflectionUI, javaConstructor,
					paramType, paramAnnotations, i));
		}
		return result;
	}

	@Override
	public Object invoke(Object object, Map<Integer, Object> valueByParameterPosition) {
		List<Object> args = new ArrayList<Object>();
		for (IParameterInfo param : getParameters()) {
			if (valueByParameterPosition.containsKey(param.getPosition())) {
				args.add(valueByParameterPosition.get(param.getPosition()));
			} else {
				args.add(param.getDefaultValue());
			}
		}
		try {
			return javaConstructor.newInstance(args.toArray());
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
		if (!javaConstructor
				.equals(((DefaultConstructorMethodInfo) obj).javaConstructor)) {
			return false;
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getDocumentation() {
		return ReflectionUIUtils.getAnnotatedInfoDocumentation(javaConstructor);
	}

	@Override
	public IModification getUndoModification(Object object, Map<Integer, Object> valueByParameterPosition) {
		return null;
	}

	@Override
	public void validateParameters(Object object,
			Map<Integer, Object> valueByParameterPosition) throws Exception {
	}

}
