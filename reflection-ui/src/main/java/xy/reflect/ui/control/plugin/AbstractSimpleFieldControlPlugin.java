


package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.util.ClassUtils;

/**
 * Base class of simple field control plugins.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSimpleFieldControlPlugin implements IFieldControlPlugin {

	/**
	 * @param javaType The tested java type.
	 * @return Whether the current plugin is compatible with the given java type.
	 */
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

	@Override
	public IFieldControlData filterDistinctNullValueControlData(Object renderer, IFieldControlData controlData) {
		return controlData;
	}

}
