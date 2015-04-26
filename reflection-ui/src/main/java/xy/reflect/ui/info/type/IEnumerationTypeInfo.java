package xy.reflect.ui.info.type;

public interface IEnumerationTypeInfo extends ITypeInfo {
	
	Object[] getPossibleValues();
	
	String formatEnumerationItem(Object object);
}
