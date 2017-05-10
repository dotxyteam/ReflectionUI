package xy.reflect.ui.control;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.undo.ModificationStack;

public interface IFieldControlInput {

	IFieldControlInput NULL_CONTROL_INPUT = new IFieldControlInput() {

		@Override
		public IInfo getModificationsTarget() {
			return IFieldInfo.NULL_FIELD_INFO;
		}

		@Override
		public ModificationStack getModificationStack() {
			return new ModificationStack(null);
		}

		@Override
		public IFieldControlData getControlData() {
			return IFieldControlData.NULL_CONTROL_DATA;
		}

		@Override
		public IContext getContext() {
			return new FieldContext(IFieldInfo.NULL_FIELD_INFO.getType(), IFieldInfo.NULL_FIELD_INFO);
		}
	};

	IFieldControlData getControlData();

	IInfo getModificationsTarget();

	ModificationStack getModificationStack();

	IContext getContext();

}