


package xy.reflect.ui.undo;

/**
 * Base class for modification proxies.
 * 
 * @author olitank
 *
 */
public abstract class AbstractModificationProxy implements IModification {
	protected IModification base;

	public AbstractModificationProxy(IModification base) {
		super();
		this.base = base;
	}

	public boolean isNull() {
		return base.isNull();
	}

	public boolean isFake() {
		return base.isFake();
	}

	public String getTitle() {
		return base.getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		AbstractModificationProxy other = (AbstractModificationProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModificationProxy [base=" + base + "]";
	}

}
