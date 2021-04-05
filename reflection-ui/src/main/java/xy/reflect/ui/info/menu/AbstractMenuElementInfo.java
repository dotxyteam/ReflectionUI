package xy.reflect.ui.info.menu;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMenuElementInfo implements IMenuElementInfo {

	private Map<String, Object> specificProperties = new HashMap<String, Object>();

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getOnlineHelp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return specificProperties;
	}

	public void setSpecificProperties(Map<String, Object> specificProperties) {
		this.specificProperties = specificProperties;
	}

}
