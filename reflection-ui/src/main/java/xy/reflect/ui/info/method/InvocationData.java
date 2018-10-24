package xy.reflect.ui.info.method;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;

public class InvocationData implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();
	protected Map<Integer, Object> defaultValueByParameterPosition = new HashMap<Integer, Object>();

	public InvocationData(List<IParameterInfo> parameters, Object... parameterValues) {
		for (IParameterInfo param : parameters) {
			defaultValueByParameterPosition.put(param.getPosition(), param.getDefaultValue());
		}
		for (int i = 0; i < parameterValues.length; i++) {
			valueByParameterPosition.put(i, parameterValues[i]);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (defaultValueByParameterPosition == null) {
			defaultValueByParameterPosition = new HashMap<Integer, Object>();
		}
	}

	public InvocationData(IMethodInfo method, Object... parameterValues) {
		this(method.getParameters(), parameterValues);
	}

	public Map<Integer, Object> getProvidedParameterValues() {
		return valueByParameterPosition;
	}

	public void setProvidedParameterValues(Map<Integer, Object> providedValues) {
		this.valueByParameterPosition = providedValues;
	}

	public Object getParameterValue(int parameterPosition) {
		if (valueByParameterPosition.containsKey(parameterPosition)) {
			return valueByParameterPosition.get(parameterPosition);
		} else {
			return defaultValueByParameterPosition.get(parameterPosition);
		}
	}

	public void provideParameterValue(int parameterPosition, Object value) {
		valueByParameterPosition.put(parameterPosition, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((defaultValueByParameterPosition == null) ? 0 : defaultValueByParameterPosition.hashCode());
		result = prime * result + ((valueByParameterPosition == null) ? 0 : valueByParameterPosition.hashCode());
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
		InvocationData other = (InvocationData) obj;
		if (defaultValueByParameterPosition == null) {
			if (other.defaultValueByParameterPosition != null)
				return false;
		} else if (!defaultValueByParameterPosition.equals(other.defaultValueByParameterPosition))
			return false;
		if (valueByParameterPosition == null) {
			if (other.valueByParameterPosition != null)
				return false;
		} else if (!valueByParameterPosition.equals(other.valueByParameterPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new HashMap<Integer, Object>(valueByParameterPosition).toString();
	}

}
