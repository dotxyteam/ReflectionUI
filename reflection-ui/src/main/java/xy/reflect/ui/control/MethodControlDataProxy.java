/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class MethodControlDataProxy implements IMethodControlData {

	protected IMethodControlData base;

	public MethodControlDataProxy(IMethodControlData base) {
		super();
		this.base = base;
	}

	public InvocationData createInvocationData(Object... parameterValues) {
		return base.createInvocationData(parameterValues);
	}

	public Object createParametersObject(InvocationData invocationData, String contextId) {
		return base.createParametersObject(invocationData, contextId);
	}

	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	public ResourcePath getBackgroundImagePath() {
		return base.getBackgroundImagePath();
	}

	public ColorSpecification getBackgroundColor() {
		return base.getBackgroundColor();
	}

	public ColorSpecification getForegroundColor() {
		return base.getForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return base.getBorderColor();
	}

	@Override
	public String getConfirmationMessage(InvocationData invocationData) {
		return base.getConfirmationMessage(invocationData);
	}

	public boolean isNullReturnValueDistinct() {
		return base.isNullReturnValueDistinct();
	}

	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	public boolean isReturnValueIgnored() {
		return base.isReturnValueIgnored();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(InvocationData invocationData) {
		return base.invoke(invocationData);
	}

	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	public Runnable getNextUpdateCustomUndoJob(InvocationData invocationData) {
		return base.getNextUpdateCustomUndoJob(invocationData);
	}

	public void validateParameters(InvocationData invocationData) throws Exception {
		base.validateParameters(invocationData);
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getMethodSignature() {
		return base.getMethodSignature();
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		MethodControlDataProxy other = (MethodControlDataProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodControlDataProxy [base=" + base + "]";
	}

}
