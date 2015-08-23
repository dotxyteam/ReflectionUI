package xy.reflect.ui.info.method;

import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;

public class InvocationData {

	private Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();

	public InvocationData() {
		this(new Object[0]);
	}

	public InvocationData(Object... parameterValues) {
		for (int i = 0; i < parameterValues.length; i++) {
			valueByParameterPosition.put(i, parameterValues[i]);
		}
	}

	public Object getParameterValue(IParameterInfo param) {
		if (valueByParameterPosition.containsKey(param.getPosition())) {
			return valueByParameterPosition.get(param.getPosition());
		} else {
			return param.getDefaultValue();
		}
	}

	public void setparameterValue(IParameterInfo param, Object value) {
		setparameterValue(param.getPosition(), value);
	}

	public void setparameterValue(int parameterPosition, Object value) {
		valueByParameterPosition.put(parameterPosition, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((valueByParameterPosition == null) ? 0
						: valueByParameterPosition.hashCode());
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
		if (valueByParameterPosition == null) {
			if (other.valueByParameterPosition != null)
				return false;
		} else if (!valueByParameterPosition
				.equals(other.valueByParameterPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new HashMap<Integer, Object>(valueByParameterPosition).toString();
	}

}
