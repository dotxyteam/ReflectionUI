package xy.reflect.ui.info.method;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import xy.reflect.ui.info.parameter.IParameterInfo;

public class InvocationData implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();

	public InvocationData() {
		this(new Object[0]);
	}

	public InvocationData(Object... parameterValues) {
		for (int i = 0; i < parameterValues.length; i++) {
			valueByParameterPosition.put(i, parameterValues[i]);
		}
	}

	public Map<Integer, Object> getValueByParameterPosition() {
		return valueByParameterPosition;
	}

	public void setValueByParameterPosition(Map<Integer, Object> valueByParameterPosition) {
		this.valueByParameterPosition = valueByParameterPosition;
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

	public void setParameterValue(IParameterInfo param, Object value) {
		setParameterValue(param.getPosition(), value);
	}

	public void setParameterValue(int parameterPosition, Object value) {
		valueByParameterPosition.put(parameterPosition, value);
	}

	public Set<Integer> getPositions() {
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
