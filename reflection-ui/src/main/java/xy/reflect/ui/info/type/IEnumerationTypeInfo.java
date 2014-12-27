package xy.reflect.ui.info.type;

import java.util.List;

public interface IEnumerationTypeInfo extends ITypeInfo {
	
	List<?> getPossibleValues();
	
	String formatValue(Object value);
}
