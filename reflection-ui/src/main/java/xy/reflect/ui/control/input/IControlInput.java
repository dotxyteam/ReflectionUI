package xy.reflect.ui.control.input;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.undo.ModificationStack;

public interface IControlInput {

	IControlData getControlData();

	IInfo getModificationsTarget();

	ModificationStack getModificationStack();

}