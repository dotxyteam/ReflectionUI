package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ICommonInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IMethodInfo extends ICommonInfo {

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(Object object, Map<String, Object> valueByParameterName);

	boolean isReadOnly();

	InfoCategory getCategory();

}
