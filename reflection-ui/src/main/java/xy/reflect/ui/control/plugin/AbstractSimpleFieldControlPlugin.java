package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.util.ClassUtils;

public abstract class AbstractSimpleFieldControlPlugin implements IFieldControlPlugin {

	protected abstract boolean handles(Class<?> javaType);

	@Override
	public String getIdentifier() {
		return getClass().getName();
	}

	@Override
	public boolean handles(IFieldControlInput input) {
		final Class<?> javaType;
		try {
			javaType = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (!handles(javaType)) {
			return false;
		}
		return true;
	}

}