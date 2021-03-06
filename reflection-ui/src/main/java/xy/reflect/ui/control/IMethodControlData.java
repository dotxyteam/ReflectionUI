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
package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface provides what UI method controls need to look and behave
 * properly.
 */
public interface IMethodControlData {

	/**
	 * @return UI-oriented return value type properties of the current method.
	 */
	ITypeInfo getReturnValueType();

	/**
	 * @return the parameters of the underlying method.
	 */
	List<IParameterInfo> getParameters();

	/**
	 * 
	 * @param invocationData
	 *            The parameter values of the the underlying method invocation.
	 * @return the result of the underlying method execution.
	 */
	Object invoke(InvocationData invocationData);

	/**
	 * @return true if and only if the execution of the underlying method is not
	 *         supposed to affect the object on which it is executed.
	 */
	boolean isReadOnly();

	/**
	 * @return a text that should be displayed by the method control to describe the
	 *         null return value.
	 */
	String getNullReturnValueLabel();

	/**
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @return a job that can revert the next invocation of the underlying method or
	 *         null if the method execution cannot be reverted.
	 */
	Runnable getNextUpdateCustomUndoJob(InvocationData invocationData);

	/**
	 * Validates the values of the method parameters. An exception is thrown if the
	 * parameter values are not valid. Otherwise the values are considered as valid.
	 * 
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @throws Exception
	 *             If the parameter values are not valid.
	 */
	void validateParameters(InvocationData invocationData) throws Exception;

	/**
	 * @return the value return mode of the underlying method. It may impact the
	 *         behavior of the control used to display the return value.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the help text of this method control data.
	 */
	String getOnlineHelp();

	/**
	 * @return custom properties intended to be used to extend this method data for
	 *         specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * @return the displayed name of this method control data.
	 */
	String getCaption();

	/**
	 * @return an identifier used to identify this method control data.
	 */
	String getInvocationIdentifier();

	/**
	 * @return true if and only if the return values of the underlying method should
	 *         be displayed in a non-blocking view, usually a stand-alone window.
	 */
	boolean isReturnValueDetached();

	/**
	 * @return true if and only if the control displaying the return value of the
	 *         underlying method must distinctly display the null value. This is
	 *         usually needed if a null return value has a special meaning different
	 *         from "empty/default value" for the developer.
	 */
	boolean isNullReturnValueDistinct();

	/**
	 * @return the location of an image resource displayed on the method control or
	 *         null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return true if and only if the underlying method return value should be
	 *         ignored.
	 */
	boolean isReturnValueIgnored();

	/**
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @return a confirmation message to be displayed just before running the
	 *         underlying method invocation so that the user will be able to cancel
	 *         the execution.
	 */
	String getConfirmationMessage(InvocationData invocationData);

	/**
	 * @return the resource location of a background image that must be displayed on
	 *         the method control.
	 */
	ResourcePath getBackgroundImagePath();

	/**
	 * @return the background color of the method control.
	 */
	ColorSpecification getBackgroundColor();

	/**
	 * @return the foreground color of the method control.
	 */
	ColorSpecification getForegroundColor();

	/**
	 * @return the border color of the method control.
	 */
	ColorSpecification getBorderColor();

	/**
	 * @param parameterValues
	 *            The parameter values used to fill the invocation data.
	 * @return the invocation data object filled with the given parameter values.
	 */
	InvocationData createInvocationData(Object... parameterValues);

	/**
	 * @param invocationData
	 *            The given invocation data.
	 * @param contextId
	 *            An identifier used to build the type of the returned object.
	 * @return an object created to edit the given invocation data.
	 */
	Object createParametersObject(InvocationData invocationData, String contextId);

	/**
	 * @return the text displayed on the validation control of the parameters
	 *         settings dialog or null if the default text should be used.
	 */
	String getParametersValidationCustomCaption();
}
