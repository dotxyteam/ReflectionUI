package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class MethodInfoProxy implements IMethodInfo {

	protected IMethodInfo base;

	public MethodInfoProxy(IMethodInfo delegate) {
		this.base = delegate;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(Object object, Map<String, Object> valueByParameterName) {
		return base.invoke(object, valueByParameterName);
	}

	@Override
	public String toString() {
		return base.toString();
	}
	
	@Override
	public int hashCode() {
		return base.hashCode();
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
		return base.equals(((MethodInfoProxy) obj).base);
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	@Override
	public String getCategoryCaption() {
		return base.getCategoryCaption();
	}


}
