package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;

public abstract class AbstractListProperty implements IFieldInfo {

	public boolean isEnabled() {
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getName() {
		return getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

}
