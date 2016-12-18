package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public class InfoFilterProxy implements IInfoFilter {

	IInfoFilter delegate;

	public InfoFilterProxy(IInfoFilter delegate) {
		super();
		this.delegate = delegate;
	}

	public boolean excludeField(IFieldInfo field) {
		return delegate.excludeField(field);
	}

	public boolean excludeMethod(IMethodInfo method) {
		return delegate.excludeMethod(method);
	}
	
	
	
}
