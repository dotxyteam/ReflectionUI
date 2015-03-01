package xy.reflect.ui.info;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public class InfoCollectionSettingsProxy implements IInfoCollectionSettings {

	IInfoCollectionSettings delegate;

	public InfoCollectionSettingsProxy(IInfoCollectionSettings delegate) {
		super();
		this.delegate = delegate;
	}

	public boolean allReadOnly() {
		return delegate.allReadOnly();
	}

	public boolean excludeField(IFieldInfo field) {
		return delegate.excludeField(field);
	}

	public boolean excludeMethod(IMethodInfo method) {
		return delegate.excludeMethod(method);
	}
	
	
	
}
