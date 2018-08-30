package xy.reflect.ui.control;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.undo.ModificationStack;

public interface IFieldControlInput {

	IFieldControlData getControlData();

	IInfo getModificationsTarget();

	ModificationStack getModificationStack();

	IContext getContext();

}