package xy.reflect.ui.control.input;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.undo.ModificationStack;

public interface IMethodControlInput {
	
	IMethodControlData getControlData();

	IInfo getModificationsTarget();

	ModificationStack getModificationStack();
}
