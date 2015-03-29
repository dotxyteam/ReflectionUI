package xy.reflect.ui.undo;

public  class ModificationProxy implements IModification {
	IModification delegate;
	ModificationProxyConfiguration configuration;

	public ModificationProxy(IModification delegate,
			ModificationProxyConfiguration configuration) {
		super();
		this.delegate = delegate;
		this.configuration = configuration;
	}

	final public IModification applyAndGetOpposite(boolean refreshView) {
		try {
			return new ModificationProxy(
					delegate.applyAndGetOpposite(refreshView),
					configuration);
		} finally {
			configuration.executeAfterApplication();
		}
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

}
