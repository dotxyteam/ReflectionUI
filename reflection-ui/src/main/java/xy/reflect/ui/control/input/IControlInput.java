package xy.reflect.ui.control.input;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.undo.ModificationStack;

public interface IControlInput {

	IControlData getControlData();

	IFieldInfo getField();

	ModificationStack getModificationStack();

}