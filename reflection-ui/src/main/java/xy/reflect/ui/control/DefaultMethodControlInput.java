package xy.reflect.ui.control;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Default method control input. It just uses the given object and method to
 * provide the features needed by the controls.
 * 
 * @author olitank
 *
 */
public class DefaultMethodControlInput implements IMethodControlInput {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IMethodInfo method;

	public DefaultMethodControlInput(ReflectionUI reflectionUI, Object object, IMethodInfo method) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.method = method;
	}

	@Override
	public IMethodControlData getControlData() {
		return new DefaultMethodControlData(reflectionUI, object, method);
	}

	@Override
	public ModificationStack getModificationStack() {
		return new ModificationStack(null);
	}

	@Override
	public IContext getContext() {
		return IContext.NULL_CONTEXT;
	}

}
