package xy.reflect.ui.info.parameter;

import xy.reflect.ui.info.ICommonInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IParameterInfo extends ICommonInfo{

	ITypeInfo getType();

	boolean isNullable();

	Object getDefaultValue();
	
	int getPosition();

}
