package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldControlData {

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

	ColorSpecification getForegroundColor();

	Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor);
}
