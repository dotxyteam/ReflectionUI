package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultConstructorMethodInfo extends AbstractConstructorMethodInfo {

	protected Constructor<?> javaConstructor;
	protected ReflectionUI reflectionUI;
	protected ArrayList<IParameterInfo> parameters;

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
	public String getName() {
		return javaConstructor.getDeclaringClass().getSimpleName();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaConstructor.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!DefaultParameterInfo.isCompatibleWith(new Parameter(
						javaConstructor, i))) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI,
						new Parameter(javaConstructor, i)));
			}
			sortParameters(parameters);
		}
		return parameters;
	}

	protected void sortParameters(List<IParameterInfo> list) {
		Collections.sort(list, new Comparator<IParameterInfo>() {
			@Override
			public int compare(IParameterInfo p1, IParameterInfo p2) {
				int result;

				result = ReflectionUIUtils.compareNullables(p1.getType()
						.getName().toUpperCase(), p2.getType().getName()
						.toUpperCase());
				if (result != 0) {
					return result;
				}

				result = ReflectionUIUtils.compareNullables(p1.getName(),
						p2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
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
	public String getOnlineHelp() {
		return ReflectionUIUtils.getAnnotatedInfoOnlineHelp(javaConstructor);
	}

	@Override
	public IModification getUndoModification(Object object,
			InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData)
			throws Exception {
	}

	public static boolean isCompatibleWith(Constructor<?> constructor) {
		if (ReflectionUIUtils.isInfoHidden(constructor)) {
			return false;
		}
		return true;
	}

}
