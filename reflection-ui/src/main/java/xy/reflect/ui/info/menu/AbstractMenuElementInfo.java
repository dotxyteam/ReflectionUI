/*
 * 
 */
package xy.reflect.ui.info.menu;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class of menu model element specifications.
 * 
 * @author olitank
 *
 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((specificProperties == null) ? 0 : specificProperties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMenuElementInfo other = (AbstractMenuElementInfo) obj;
		if (specificProperties == null) {
			if (other.specificProperties != null)
				return false;
		} else if (!specificProperties.equals(other.specificProperties))
			return false;
		return true;
	}

}
