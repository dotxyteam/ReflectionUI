package xy.reflect.ui.info.type.iterable.map;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IMapEntryTypeInfo extends ITypeInfo{

	IFieldInfo getKeyField();
	IFieldInfo getValueField();

}
