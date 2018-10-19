package xy.reflect.ui.info.method;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;

public class InvocationData implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<Integer, Object> providedParameterValues = new HashMap<Integer, Object>();
	protected List<IParameterInfo> parameters;

	public InvocationData(List<IParameterInfo> parameters, Object... parameterValues) {
		this.parameters = parameters;
		for (int i = 0; i < parameterValues.length; i++) {
			providedParameterValues.put(i, parameterValues[i]);
		}
	}

	public InvocationData(IMethodInfo method, Object... parameterValues) {
		this(method.getParameters(), parameterValues);
	}

	public Map<Integer, Object> getProvidedParameterValues() {
		return providedParameterValues;
	}

	public void setProvidedParameterValues(Map<Integer, Object> providedValues) {
		this.providedParameterValues = providedValues;
	}

	public Object getParameterValue(int parameterPosition) {
		if (providedParameterValues.containsKey(parameterPosition)) {
			return providedParameterValues.get(parameterPosition);
		} else {
			return parameters.get(parameterPosition).getDefaultValue();
		}
	}

	public void provideParameterValue(int parameterPosition, Object value) {
		providedParameterValues.put(parameterPosition, value);
	}

	public boolean areAllDefaultValuesProvided() {
		for (IParameterInfo param : parameters) {
			if (param.getDefaultValue() == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((providedParameterValues == null) ? 0 : providedParameterValues.hashCode());
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
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (providedParameterValues == null) {
			if (other.providedParameterValues != null)
				return false;
		} else if (!providedParameterValues.equals(other.providedParameterValues))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new HashMap<Integer, Object>(providedParameterValues).toString();
	}

}
