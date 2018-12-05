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

public interface IMethodControlData {

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(InvocationData invocationData);

	boolean isReadOnly();

	String getNullReturnValueLabel();

	Runnable getNextUpdateCustomUndoJob(InvocationData invocationData);

	void validateParameters(InvocationData invocationData) throws Exception;

	ValueReturnMode getValueReturnMode();

	String getOnlineHelp();

	Map<String, Object> getSpecificProperties();

	String getCaption();

	String getMethodSignature();

	boolean isReturnValueDetached();

	boolean isNullReturnValueDistinct();

	ResourcePath getIconImagePath();

	boolean isReturnValueIgnored();

	String getConfirmationMessage(InvocationData invocationData);

	ResourcePath getBackgroundImagePath();

	ColorSpecification getBackgroundColor();

	ColorSpecification getForegroundColor();

	ColorSpecification getBorderColor();

	InvocationData createInvocationData(Object... parameterValues);

	Object createParametersObject(InvocationData invocationData, String contextId);

	String getParametersValidationCustomCaption();
}
