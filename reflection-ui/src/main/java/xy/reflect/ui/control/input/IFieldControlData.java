package xy.reflect.ui.control.input;

import java.util.Map;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldControlData {

	IFieldControlData NULL_CONTROL_DATA = new DefaultFieldControlData(null, IFieldInfo.NULL_FIELD_INFO){

		@Override
		public String toString() {
			return "NULL_CONTROL_DATA";
		}
		
	};

	Object getValue();

	void setValue(Object value);

	String getCaption();
	
	Runnable getCustomUndoUpdateJob(Object value);

	ITypeInfo getType();

	boolean isGetOnly();

	ValueReturnMode getValueReturnMode();

	boolean isNullable();

	String getNullValueLabel();

	Map<String, Object> getSpecificProperties();
}
