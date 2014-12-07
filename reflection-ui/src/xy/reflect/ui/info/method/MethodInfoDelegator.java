package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class MethodInfoDelegator implements IMethodInfo {

	protected IMethodInfo delegate;

	public MethodInfoDelegator(IMethodInfo delegate) {
		this.delegate = delegate;
	}

	public String getName() {
		return delegate.getName();
	}

	public String getCaption() {
		return delegate.getCaption();
	}

	public ITypeInfo getReturnValueType() {
		return delegate.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return delegate.getParameters();
	}

	public Object invoke(Object object, Map<String, Object> valueByParameterName) {
		return delegate.invoke(object, valueByParameterName);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		if(!getClass().equals(obj.getClass())){
			return false;
		}
		return delegate.equals(((MethodInfoDelegator) obj).delegate);
	}

	@Override
	public boolean isReadOnly() {
		return delegate.isReadOnly();
	}


}
