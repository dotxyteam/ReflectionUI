


package xy.reflect.ui.info;

/**
 * Base class of abstract UI model proxies. It ensures that the 'name' of the
 * proxy is checked when computing the equality of 2 instances.
 * 
 * @author olitank
 *
 */
public abstract class AbstractInfoProxy extends AbstractInfo {

	@Override
	public int hashCode() {
		return ((getName() == null) ? 0 : getName().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractInfoProxy other = (AbstractInfoProxy) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

}
