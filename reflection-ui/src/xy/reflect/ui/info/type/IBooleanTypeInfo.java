package xy.reflect.ui.info.type;

public interface IBooleanTypeInfo extends ITypeInfo {
	Boolean toBoolean(Object value);

	Object fromBoolean(Boolean b);
}
