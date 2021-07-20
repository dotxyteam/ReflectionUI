


package xy.reflect.ui.control;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Default field control input. It just uses the given object and field to
 * provide the features needed by the controls.
 * 
 * @author olitank
 *
 */
public class DefaultFieldControlInput implements IFieldControlInput {

	private ReflectionUI reflectionUI;
	private Object object;
	private IFieldInfo field;

	public DefaultFieldControlInput(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
	}

	public DefaultFieldControlInput(ReflectionUI reflectionUI) {
		this(reflectionUI, null, IFieldInfo.NULL_FIELD_INFO);
	}

	@Override
	public ModificationStack getModificationStack() {
		return new ModificationStack(null);
	}

	@Override
	public IFieldControlData getControlData() {
		return new DefaultFieldControlData(reflectionUI, object, field);
	}

	@Override
	public IContext getContext() {
		return IContext.NULL_CONTEXT;
	}

}
