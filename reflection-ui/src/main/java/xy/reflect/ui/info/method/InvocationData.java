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
}
