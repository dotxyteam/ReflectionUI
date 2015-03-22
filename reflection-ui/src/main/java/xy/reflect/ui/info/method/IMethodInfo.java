package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.control.ModificationStack.IModification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IMethodInfo extends IInfo {

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(Object object, Map<String, Object> valueByParameterName);

	boolean isReadOnly();

	InfoCategory getCategory();

	IModification getUndoModification(Object object,
			Map<String, Object> valueByParameterName);

	void validateParameters(Object object,
			Map<String, Object> valueByParameterName) throws Exception;

}
