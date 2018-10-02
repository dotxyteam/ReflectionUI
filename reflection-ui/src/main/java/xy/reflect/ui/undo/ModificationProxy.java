package xy.reflect.ui.undo;

public class ModificationProxy implements IModification {
	protected IModification delegate;

	public ModificationProxy(IModification delegate) {
		super();
		this.delegate = delegate;
	}

	public IModification applyAndGetOpposite() {
		return delegate.applyAndGetOpposite();
	}

	public boolean isNull() {
		return delegate.isNull();
	}

	public String getTitle() {
		return delegate.getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		ModificationProxy other = (ModificationProxy) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModificationProxy [delegate=" + delegate + "]";
	}

}
