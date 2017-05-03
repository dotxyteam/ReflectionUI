package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PresetInvocationDataMethod extends MethodInfoProxy {

	protected InvocationData invocationData;

	public PresetInvocationDataMethod(IMethodInfo base, InvocationData invocationData) {
		super(base);
		this.invocationData = invocationData;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return super.invoke(object, this.invocationData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PresetInvocationDataMethod other = (PresetInvocationDataMethod) obj;
		if (invocationData == null) {
			if (other.invocationData != null)
				return false;
		} else if (!invocationData.equals(other.invocationData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PresetInvocationDataMethod [base=" + base + ", invocationData=" + invocationData + "]";
	}
	
	

}