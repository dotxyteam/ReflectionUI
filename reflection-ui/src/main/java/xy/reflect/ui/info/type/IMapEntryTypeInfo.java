package xy.reflect.ui.info.type;

import xy.reflect.ui.info.field.IFieldInfo;

public interface IMapEntryTypeInfo extends ITypeInfo{

	IFieldInfo getKeyField();
	IFieldInfo getValueField();

}
