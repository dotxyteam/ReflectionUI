package xy.reflect.ui.info;

import java.util.Map;

public interface IInfo {
	String getName();

	String getCaption();

	String getOnlineHelp();
	
	Map<String, Object> getSpecificProperties();
	
}
