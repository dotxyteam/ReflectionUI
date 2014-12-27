package xy.reflect.ui.info.parameter;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IParameterInfo extends IInfo{

	ITypeInfo getType();

	boolean isNullable();

	Object getDefaultValue();
	
	int getPosition();

}
