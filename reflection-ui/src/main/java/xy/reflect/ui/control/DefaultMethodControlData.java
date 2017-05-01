package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class DefaultMethodControlData implements IMethodControlData {

	protected Object object;
	protected IMethodInfo method;

	public DefaultMethodControlData(Object object, IMethodInfo method) {
		super();
		this.object = object;
		this.method = method;
	}

	@Override
	public boolean isReturnValueNullable() {
		return method.isReturnValueNullable();
	}

	@Override
	public boolean isReturnValueDetached() {
		return method.isReturnValueDetached();
	}

	public boolean isReturnValueIgnored() {
		return method.isReturnValueIgnored();
	}

	@Override
	public String getCaption() {
		return method.getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return method.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return method.getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return method.getReturnValueType();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return method.getParameters();
	}

	@Override
	public Object invoke(InvocationData invocationData) {
		return method.invoke(object, invocationData);
	}

	@Override
	public boolean isReadOnly() {
		return method.isReadOnly();
	}

	@Override
	public String getNullReturnValueLabel() {
		return method.getNullReturnValueLabel();
	}

	@Override
	public Runnable getUndoJob(InvocationData invocationData) {
		return method.getUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return method.getValueReturnMode();
	}

	@Override
	public String getMethodSignature() {
		return method.getSignature();
	}

	public String getIconImagePath() {
		return method.getIconImagePath();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		DefaultMethodControlData other = (DefaultMethodControlData) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMethodControlData [object=" + object + ", method=" + method + "]";
	}

}
