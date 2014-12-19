package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

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
		int i = 0;
		for (Class<?> paramType : javaConstructor.getParameterTypes()) {
			result.add(new DefaultParameterInfo(reflectionUI, javaConstructor,
					paramType, i));
			i++;
		}
		return result;
	}

	@Override
	public Object invoke(Object object, Map<String, Object> valueByParameterName) {
		List<Object> args = new ArrayList<Object>();
		for (IParameterInfo param : getParameters()) {
			args.add(valueByParameterName.get(param.getName()));
		}
		try {
			return javaConstructor.newInstance(args.toArray());
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (IllegalArgumentException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e.getTargetException());
		} catch (InstantiationException e) {
			throw new AssertionError(e);
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

}
