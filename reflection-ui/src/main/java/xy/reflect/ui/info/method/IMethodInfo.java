package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;

public interface IMethodInfo extends IInfo {

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(Object object, Map<Integer, Object> valueByParameterPosition);

	boolean isReadOnly();

	InfoCategory getCategory();

	IModification getUndoModification(Object object,
			Map<Integer, Object> valueByParameterPosition);

	void validateParameters(Object object,
			Map<Integer, Object> valueByParameterPosition) throws Exception;

}
