package xy.reflect.ui.info.type;

public interface ITextualTypeInfo extends ITypeInfo {
	String toText(Object value);

	Object fromText(String text);
}
