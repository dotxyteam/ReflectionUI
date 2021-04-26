/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.method;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.parameter.IParameterInfo;

/**
 * Method parameter values class.
 * 
 * @author olitank
 *
 */
public class InvocationData implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();
	protected Map<Integer, Object> defaultValueByParameterPosition = new HashMap<Integer, Object>();

	public InvocationData(Object object, List<IParameterInfo> parameters, Object... parameterValues) {
		for (IParameterInfo param : parameters) {
			defaultValueByParameterPosition.put(param.getPosition(), param.getDefaultValue(object));
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

	public InvocationData(Object object, IMethodInfo method, Object... parameterValues) {
		this(object, method.getParameters(), parameterValues);
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

	public void provideParameterValue(int parameterPosition, Object value) {
		valueByParameterPosition.put(parameterPosition, value);
	}

	public void withdrawParameterValue(int parameterPosition) {
		valueByParameterPosition.remove(parameterPosition);
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
		return "ProvidedValues=" + valueByParameterPosition + ", DefaultValues=" + defaultValueByParameterPosition;
	}

}
