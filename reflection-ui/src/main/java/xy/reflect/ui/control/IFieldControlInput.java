package xy.reflect.ui.control;

import xy.reflect.ui.undo.ModificationStack;

public interface IFieldControlInput {

	IFieldControlData getControlData();

	ModificationStack getModificationStack();

	IContext getContext();

}