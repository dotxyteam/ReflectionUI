package xy.reflect.ui.info.type;

import java.util.List;

public interface IEnumerationTypeInfo extends ITypeInfo {
	
	List<?> getPossibleValues(Object object);
	
	String formatEnumerationItem(Object object);
}
