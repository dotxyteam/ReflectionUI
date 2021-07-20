


package xy.reflect.ui.info.method;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;

/**
 * Method parameter values class. Parameters are referenced by their positions.
 * The class holds the default and chosen (provided) parameter values. If a
 * parameter value is not provided then the default value is used. If neither of
 * these values is provided then the null value is used.
 * 
 * @author olitank
 *
 */
public class InvocationData implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();
	protected Map<Integer, Object> defaultValueByParameterPosition = new HashMap<Integer, Object>();

	public InvocationData(InvocationData invocationData) {
		valueByParameterPosition.putAll(invocationData.valueByParameterPosition);
		defaultValueByParameterPosition.putAll(invocationData.defaultValueByParameterPosition);
	}

	public InvocationData(Object object, List<IParameterInfo> parameters, Object... parameterValues) {
		for (IParameterInfo param : parameters) {
			defaultValueByParameterPosition.put(param.getPosition(), param.getDefaultValue(object));
		}
		for (int i = 0; i < parameterValues.length; i++) {
			valueByParameterPosition.put(i, parameterValues[i]);
		}
	}

	public InvocationData(Object object, IMethodInfo method, Object... parameterValues) {
		this(object, method.getParameters(), parameterValues);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (defaultValueByParameterPosition == null) {
			defaultValueByParameterPosition = new HashMap<Integer, Object>();
		}
	}

	public Map<Integer, Object> getProvidedParameterValues() {
		return valueByParameterPosition;
	}

	public void setProvidedParameterValues(Map<Integer, Object> providedValues) {
		this.valueByParameterPosition = providedValues;
	}

	public Map<Integer, Object> getDefaultParameterValues() {
		return defaultValueByParameterPosition;
	}

	public void setDefaultParameterValues(Map<Integer, Object> defaultValues) {
		this.defaultValueByParameterPosition = defaultValues;
	}

	public Object getParameterValue(int parameterPosition) {
		if (valueByParameterPosition.containsKey(parameterPosition)) {
			return valueByParameterPosition.get(parameterPosition);
		} else {
			return defaultValueByParameterPosition.get(parameterPosition);
		}
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
		Map<Integer, Object> values = new HashMap<Integer, Object>();
		values.putAll(defaultValueByParameterPosition);
		values.putAll(valueByParameterPosition);
		return values.toString();
	}

}
