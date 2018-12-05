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
package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented method properties.
 * 
 * @author olitank
 *
 */
public interface IMethodInfo extends IInfo {

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 */
	public IMethodInfo NULL_METHOD_INFO = new IMethodInfo() {

		@Override
		public String getName() {
			return "NULL_METHOD_INFO";
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getParametersValidationCustomCaption() {
			return null;
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return null;
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
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.DIRECT_OR_PROXY;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return null;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		}

		@Override
		public String getNullReturnValueLabel() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	/**
	 * @return the signature of this method.
	 */
	String getSignature();

	/**
	 * @return UI-oriented return value type properties of the current method.
	 */
	ITypeInfo getReturnValueType();

	/**
	 * @return the parameters of this method.
	 */
	List<IParameterInfo> getParameters();

	/**
	 * 
	 * @param object
	 *            The object offering this method.
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @return the result of this method execution.
	 */
	Object invoke(Object object, InvocationData invocationData);

	/**
	 * @return true if and only if the execution of this method is not supposed to
	 *         affect the object on which it is executed.
	 */
	boolean isReadOnly();

	/**
	 * @return a text that should be displayed by the method control to describe the
	 *         null return value.
	 */
	String getNullReturnValueLabel();

	/**
	 * @return the category in which this method will be displayed.
	 */
	InfoCategory getCategory();

	/**
	 * @param object
	 *            The object offering this method.
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @return a job that can revert the next invocation of this method, or null if
	 *         the method execution cannot be reverted.
	 */
	Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData);

	/**
	 * Validates the values of the method parameters. An exception is thrown if the
	 * parameter values are not valid. Otherwise the values are considered as valid.
	 * 
	 * @param object
	 *            The object offering this method.
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @throws Exception
	 *             If the parameter values are not valid.
	 */
	void validateParameters(Object object, InvocationData invocationData) throws Exception;

	/**
	 * @return the value return mode of this method. It may impact the behavior of
	 *         this controls used to display the return value.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the location of an image resource displayed on the method control or
	 *         null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return true if and only if the return values of this method should be
	 *         displayed in a non-blocking view, usually a stand-alone window.
	 */
	boolean isReturnValueDetached();

	/**
	 * @return true if and only if the control displaying the return value of this
	 *         method must distinctly display the null value. This is usually needed
	 *         if a null return value has a special meaning different from
	 *         "empty/default value" for the developer.
	 */
	boolean isNullReturnValueDistinct();

	/**
	 * @return true if and only if this method return value should be ignored.
	 */
	boolean isReturnValueIgnored();

	/**
	 * @param object
	 *            The object offering this method.
	 * @param invocationData
	 *            The parameter values of the method invocation.
	 * @return a confirmation message to be displayed just before running this
	 *         method invocation so that the user will be able to cancel the
	 *         execution.
	 */
	String getConfirmationMessage(Object object, InvocationData invocationData);

	/**
	 * @return true if and only if this method control should be filtered out from
	 *         the display.
	 */
	boolean isHidden();

	/**
	 * @return the text displayed on the validation control of the parameters
	 *         settings dialog or null if the default text should be used.
	 */
	String getParametersValidationCustomCaption();

}
