package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;

public class ModificationProxy implements IModification {
	protected IModification delegate;
	protected ModificationProxyConfiguration configuration;

	public ModificationProxy(IModification delegate, ModificationProxyConfiguration configuration) {
		super();
		this.delegate = delegate;
		this.configuration = configuration;
	}

	final public IModification applyAndGetOpposite() {
		try {
			return new ModificationProxy(delegate.applyAndGetOpposite(), configuration);
		} finally {
			configuration.executeAfterApplication();
		}
	}

	public IInfo getTarget() {
		return delegate.getTarget();
	}

	final public int getNumberOfUnits() {
		return delegate.getNumberOfUnits();
	}

	final public String getTitle() {
		return delegate.getTitle();
	}

	@Override
	final public String toString() {
		return delegate.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
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
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

}
