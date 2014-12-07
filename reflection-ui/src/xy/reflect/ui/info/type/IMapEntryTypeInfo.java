package xy.reflect.ui.info.type;

public interface IMapEntryTypeInfo extends ITypeInfo{

	ITypeInfo getKeyType();
	ITypeInfo getValueType();

}
