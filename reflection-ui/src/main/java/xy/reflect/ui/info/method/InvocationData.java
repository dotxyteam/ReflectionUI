package xy.reflect.ui.info.method;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
		return getParameterValue(param.getPosition(), param.getDefaultValue());
	}

	public Object getParameterValue(int parameterPosition, Object defaultValue) {
		if (valueByParameterPosition.containsKey(parameterPosition)) {
			return valueByParameterPosition.get(parameterPosition);
		} else {
			return defaultValue;
		}
	}

	public void setparameterValue(IParameterInfo param, Object value) {
		setparameterValue(param.getPosition(), value);
	}

	public void setparameterValue(int parameterPosition, Object value) {
		valueByParameterPosition.put(parameterPosition, value);
	}

	
	public Set<Integer> getPositions(){
		return valueByParameterPosition.keySet();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
