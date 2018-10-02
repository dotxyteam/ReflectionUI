package xy.reflect.ui.control;

import xy.reflect.ui.undo.ModificationStack;

public interface IMethodControlInput {

	IMethodControlData getControlData();

	ModificationStack getModificationStack();

	IContext getContext();
}
