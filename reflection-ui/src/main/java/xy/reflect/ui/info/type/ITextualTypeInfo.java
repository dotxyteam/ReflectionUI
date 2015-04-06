package xy.reflect.ui.info.type;

public interface ITextualTypeInfo extends ITypeInfo {
	String toText(Object object);

	Object fromText(String text);
}
