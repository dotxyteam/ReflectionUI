package xy.reflect.ui.info;

import java.util.Map;

public interface IInfo {
	String getName();

	String getCaption();

	String getDocumentation();
	
	Map<String, Object> getSpecificProperties();
	
}
