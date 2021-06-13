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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.FieldAsParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IrreversibleModificationException;
import xy.reflect.ui.util.FututreActionBuilder;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Method proxy that have additional virtual parameters allowing view/update the
 * values of specified fields.
 * 
 * @author olitank
 *
 */
public class ParameterizedFieldsMethodInfo extends MethodInfoProxy {

	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> parameterizedFields;
	protected ITypeInfo containingType;
	protected FututreActionBuilder undoJobBuilder;
	protected List<FieldAsParameterInfo> generatedParameters;

	public ParameterizedFieldsMethodInfo(ReflectionUI reflectionUI, IMethodInfo method,
			List<IFieldInfo> parameterizedFields, ITypeInfo containingType) {
		super(method);
		this.reflectionUI = reflectionUI;
		this.parameterizedFields = parameterizedFields;
		this.containingType = containingType;
		this.generatedParameters = generateParameters();
	}

	protected List<FieldAsParameterInfo> generateParameters() {
		List<FieldAsParameterInfo> result = new ArrayList<FieldAsParameterInfo>();
		int startPosition = super.getParameters().size();
		for (int i = 0; i < parameterizedFields.size(); i++) {
			final IFieldInfo field = parameterizedFields.get(i);
			final int position = i + startPosition;
			result.add(new FieldAsParameterInfo(reflectionUI, field, position) {

				@Override
				public String getName() {
					return "field." + field.getName();
				}
			});
		}
		return result;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>(super.getParameters());
		result.addAll(generatedParameters);
		return result;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		InvocationData newInvocationData = new InvocationData(invocationData);
		for (FieldAsParameterInfo generatedParameter : generatedParameters) {
			Object value = invocationData.getParameterValue(generatedParameter.getPosition());
			if (undoJobBuilder != null) {
				undoJobBuilder.setOption(getUndoJobName(generatedParameter),
						ReflectionUIUtils.getNextUpdateUndoJob(object, generatedParameter.getSourceField(), value));
			}
			generatedParameter.getSourceField().setValue(object, value);
			newInvocationData.getProvidedParameterValues().remove(generatedParameter.getPosition());
			newInvocationData.getDefaultParameterValues().remove(generatedParameter.getPosition());
		}
		if (undoJobBuilder != null) {
			undoJobBuilder.setOption(getBaseMethodUndoJobName(),
					super.getNextInvocationUndoJob(object, invocationData));
			undoJobBuilder.build();
			undoJobBuilder = null;
		}
		return super.invoke(object, newInvocationData);
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		undoJobBuilder = new FututreActionBuilder();
		return undoJobBuilder.will(new FututreActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				Runnable baseMethodUndoJob = (Runnable) options.get(getBaseMethodUndoJobName());
				if (baseMethodUndoJob == null) {
					throw new IrreversibleModificationException();
				}
				baseMethodUndoJob.run();
				for (int i = generatedParameters.size() - 1; i >= 0; i--) {
					FieldAsParameterInfo generatedParameter = generatedParameters.get(i);
					Runnable fieldUndoJob = (Runnable) options.get(getUndoJobName(generatedParameter));
					fieldUndoJob.run();
				}
			}
		});
	}

	protected String getBaseMethodUndoJobName() {
		return "baseMethodUndoJob";
	}

	protected String getUndoJobName(FieldAsParameterInfo generatedParameter) {
		return "field" + generatedParameter.getPosition() + "UndoJob";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		result = prime * result + ((parameterizedFields == null) ? 0 : parameterizedFields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParameterizedFieldsMethodInfo other = (ParameterizedFieldsMethodInfo) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		if (parameterizedFields == null) {
			if (other.parameterizedFields != null)
				return false;
		} else if (!parameterizedFields.equals(other.parameterizedFields))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterizedFieldsMethodInfo [parameterizedFields=" + parameterizedFields + ", containingType="
				+ containingType + ", base=" + base + "]";
	}

}
