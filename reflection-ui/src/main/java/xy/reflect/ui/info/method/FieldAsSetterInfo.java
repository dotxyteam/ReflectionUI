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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldAsSetterInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo field;
	protected IParameterInfo parameter;

	public FieldAsSetterInfo(ReflectionUI reflectionUI, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.field = field;
		this.parameter = createParameter();
	}

	protected IParameterInfo createParameter() {
		return new ParameterInfoProxy(IParameterInfo.NULL_PARAMETER_INFO) {

			ITypeInfo type;

			@Override
			public String getName() {
				return field.getName();
			}

			@Override
			public String getCaption() {
				return field.getCaption();
			}

			@Override
			public ITypeInfo getType() {
				if (type == null) {
					type = reflectionUI.getTypeInfo(new TypeInfoSourceProxy(field.getType().getSource()) {
						@Override
						public SpecificitiesIdentifier getSpecificitiesIdentifier() {
							return null;
						}
					});
				}
				return type;
			}

			@Override
			public boolean isNullValueDistinct() {
				return field.isNullValueDistinct();
			}

			@Override
			public int getPosition() {
				return 0;
			}

			@Override
			public String toString() {
				return "Parameter [of=" + FieldAsSetterInfo.this.toString() + "]";
			}

		};
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public String getName() {
		return field.getName() + ".set";
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultMethodCaption(this);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.<IParameterInfo>singletonList(parameter);
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter.getPosition());
		field.setValue(object, value);
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return field.isTransient();
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public Runnable getNextInvocationUndoJob(final Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter.getPosition());
		Runnable result = field.getNextUpdateCustomUndoJob(object, value);
		if (result == null) {
			final Object oldValue = field.getValue(object);
			result = new Runnable() {
				@Override
				public void run() {
					field.setValue(object, oldValue);
				}
			};
		}
		return result;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
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
		FieldAsSetterInfo other = (FieldAsSetterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsSetter [field=" + field + "]";
	}

}
