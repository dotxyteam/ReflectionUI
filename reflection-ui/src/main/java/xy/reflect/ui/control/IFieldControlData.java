package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldControlData {

	IFieldControlData NULL_CONTROL_DATA = new DefaultFieldControlData(null, IFieldInfo.NULL_FIELD_INFO) {

		@Override
		public String toString() {
			return "NULL_CONTROL_DATA";
		}

	};

	Object getValue();

	void setValue(Object value);

	String getCaption();

	Runnable getNextUpdateCustomUndoJob(Object newValue);

	ITypeInfo getType();

	boolean isGetOnly();

	ValueReturnMode getValueReturnMode();

	boolean isNullValueDistinct();

	String getNullValueLabel();

	boolean isFormControlMandatory();

	boolean isFormControlEmbedded();

	IInfoFilter getFormControlFilter();

	Map<String, Object> getSpecificProperties();

	ColorSpecification getFormForegroundColor();
}
