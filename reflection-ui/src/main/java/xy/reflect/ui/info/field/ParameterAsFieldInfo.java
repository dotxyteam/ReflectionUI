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
package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;

public class ParameterAsFieldInfo extends AbstractInfo implements IFieldInfo {
	protected IParameterInfo param;
	protected IMethodInfo method;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	protected static Map<Object, Map<IMethodInfo, Map<IParameterInfo, Object>>> valueByParameterByMethodByObject = new MapMaker()
			.weakKeys().makeMap();

	public ParameterAsFieldInfo(ReflectionUI reflectionUI, IMethodInfo method, IParameterInfo param,
			ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.param = param;
		this.method = method;
		this.containingType = containingType;
	}

	protected Map<IParameterInfo, Object> getValueByParameter(Object object) {
		Map<IMethodInfo, Map<IParameterInfo, Object>> valueByParameterByMethod = valueByParameterByMethodByObject
				.get(object);
		if (valueByParameterByMethod == null) {
			valueByParameterByMethod = new HashMap<IMethodInfo, Map<IParameterInfo, Object>>();
			valueByParameterByMethodByObject.put(object, valueByParameterByMethod);
		}
		Map<IParameterInfo, Object> valueByParameter = valueByParameterByMethod.get(method);
		if (valueByParameter == null) {
			valueByParameter = new HashMap<IParameterInfo, Object>();
			valueByParameterByMethod.put(method, valueByParameter);
		}
		return valueByParameter;
	}

	@Override
	public Object getValue(Object object) {
		Map<IParameterInfo, Object> valueByParameter = getValueByParameter(object);
		if (!valueByParameter.containsKey(param)) {
			return param.getDefaultValue(object);
		}
		return valueByParameter.get(param);
	}

	@Override
	public void setValue(Object object, Object value) {
		getValueByParameter(object).put(param, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isNullValueDistinct() {
		return param.isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return param.getValueOptions(object);
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new TypeInfoSourceProxy(param.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(containingType.getName(), getName());
				}
			});
		}
		return type;
	}

	@Override
	public String getCaption() {
		return param.getCaption();
	}

	@Override
	public boolean isGetOnly() {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public String getName() {
		return param.getName();
	}

	@Override
	public boolean isHidden() {
		return param.isHidden();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return param.getOnlineHelp();
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	public IMethodInfo getReducedParameterListMethod() {
		return new MethodInfoProxy(method) {

			@Override
			public List<IParameterInfo> getParameters() {
				List<IParameterInfo> result = new ArrayList<IParameterInfo>();
				for (IParameterInfo param : super.getParameters()) {
					if (ParameterAsFieldInfo.this.param.getName().equals(param.getName())) {
						continue;
					}
					result.add(param);
				}
				return result;
			}

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				Object paramValue = ParameterAsFieldInfo.this.getValue(object);
				invocationData.provideParameterValue(param.getPosition(), paramValue);
				try {
					super.validateParameters(object, invocationData);
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
				return super.invoke(object, invocationData);
			}

			@Override
			public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
				Object paramValue = ParameterAsFieldInfo.this.getValue(object);
				invocationData.provideParameterValue(param.getPosition(), paramValue);
				return super.getNextInvocationUndoJob(object, invocationData);
			}

		};

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((param == null) ? 0 : param.hashCode());
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
		ParameterAsFieldInfo other = (ParameterAsFieldInfo) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (param == null) {
			if (other.param != null)
				return false;
		} else if (!param.equals(other.param))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterAsField [method=" + method + ", param=" + param + "]";
	}

}
