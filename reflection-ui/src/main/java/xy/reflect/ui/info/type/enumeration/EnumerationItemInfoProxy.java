


package xy.reflect.ui.info.type.enumeration;

import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.ResourcePath;

/**
 * Enumeration item information proxy class. The methods in this class should be overriden to
 * provide custom information.
 * 
 * @author olitank
 *
 */
public class EnumerationItemInfoProxy extends AbstractInfoProxy implements IEnumerationItemInfo {

	protected IEnumerationItemInfo base;

	public EnumerationItemInfoProxy(IEnumerationItemInfo base) {
		this.base = base;
	}

	public Object getValue() {
		return base.getValue();
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnumerationItemInfoProxy other = (EnumerationItemInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EnumerationItemInfoProxy [base=" + base + "]";
	}

}
