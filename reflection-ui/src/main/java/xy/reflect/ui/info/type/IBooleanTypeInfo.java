package xy.reflect.ui.info.type;

public interface IBooleanTypeInfo extends ITypeInfo {
	Boolean toBoolean(Object object);

	Object fromBoolean(Boolean b);
}
