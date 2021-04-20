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

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface provides what UI field controls need to look and behave
 * properly. <br>
 * <br>
 * About colors:
 * <ul>
 * <li>there are 4 colors that controls must take into account:</li>
 * <ul>
 * <li>label foreground color (usually the color of a non-editable text
 * describing the control)</li>
 * <li>editor background color (usually the background color of an editable
 * text)</li>
 * <li>editor foreground color (usually the color of an editable text)</li>
 * <li>border color
 * </ul>
 * <li>note that the controls have 2 states that must be distinctly displayed
 * while respecting the above colors:</li>
 * <ul>
 * <li>editable</li>
 * <li>not editable</li>
 * </ul>
 * <li>normally when 1 of these colors is not specified, the control must
 * replace it by its natural color</li>
 * <li>It means there is typically 2 possible color combinations at the same
 * area of the control:</li>
 * <ul>
 * <li>the label foreground color above transparency (not editable)</li>
 * <li>the editor foreground color above the editor background color
 * (editable)</li>
 * </ul>
 * <li>In case the control cannot conform to the specified colors, it is
 * tolerated that it gets its natural colors</li>
 * </ul>
 * 
 * @author olitank
 *
 */
public interface IFieldControlData {

	/**
	 * @return the value that the control must display.
	 */
	Object getValue();

	/**
	 * Updates the value that the control displays.
	 * 
	 * @param value The new value.
	 */
	void setValue(Object value);

	/**
	 * @return the displayed name of this control value.
	 */
	String getCaption();

	/**
	 * @param newValue The new value.
	 * @return a job that can revert the next value update or null if the default
	 *         undo job should be used.
	 */
	Runnable getNextUpdateCustomUndoJob(Object newValue);

	/**
	 * @return the help text of this field control data.
	 */
	String getOnlineHelp();

	/**
	 * @return UI-oriented type properties of the current field.
	 */
	ITypeInfo getType();

	/**
	 * @return true if and only if the control value can be updated. Then
	 *         {@link #setValue(Object)} should not be called.
	 */
	boolean isGetOnly();

	/**
	 * @return true if and only if this field value update should not be stored in a
	 *         modification stack.
	 */
	boolean isTransient();

	/**
	 * @return the value return mode of this control data. It may impact the
	 *         behavior of this control.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return true if and only if the control must distinctly display and allow to
	 *         set the null value. This is usually needed if a null value has a
	 *         special meaning different from "empty/default value" for the
	 *         developer.
	 */
	boolean isNullValueDistinct();

	/**
	 * @return a text that should be displayed by the control to describe the null
	 *         value.
	 */
	String getNullValueLabel();

	/**
	 * @return true if this control value is forcibly displayed as a generic form.
	 *         If false is returned then a custom control may be displayed. Note
	 *         that the form is either embedded in the parent form or displayed in a
	 *         child dialog according to the return value of
	 *         {@link #isFormControlEmbedded()}.
	 */
	boolean isFormControlMandatory();

	/**
	 * @return whether this control value form is embedded in the parent form or
	 *         displayed in a child dialog. Note that this method has no impact if a
	 *         custom control is displayed instead of a generic form.
	 */
	boolean isFormControlEmbedded();

	/**
	 * @return an object used to filter out some fields and methods from this field
	 *         value form. Note that this method has no impact if a custom control
	 *         is displayed instead of a generic form.
	 */
	IInfoFilter getFormControlFilter();

	/**
	 * @return custom properties intended to be used to extend the this control data
	 *         for specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * @return the color that the control must use on its transparent parts.
	 */
	ColorSpecification getLabelForegroundColor();

	/**
	 * @return the border color that the control must use.
	 */
	ColorSpecification getBorderColor();

	/**
	 * @return the background color that the control must use on its editing parts.
	 *         Note that this color must be used in combination with the foreground
	 *         color returned by {@link #getEditorForegroundColor()}.
	 */
	ColorSpecification getEditorBackgroundColor();

	/**
	 * @return the text color that the control must use on its editing parts. Note
	 *         that this color must be used in combination with the background color
	 *         returned by {@link #getEditorBackgroundColor()}.
	 */
	ColorSpecification getEditorForegroundColor();

	/**
	 * @return the path to an image that buttons of the control must use as their
	 *         background image or null if the control buttons must have the default
	 *         background.
	 */
	ResourcePath getButtonBackgroundImagePath();

	/**
	 * @return the background color that buttons of the control must use as their
	 *         background color or null if the control buttons must have the default
	 *         background color.
	 */
	ColorSpecification getButtonBackgroundColor();

	/**
	 * @return the text color that buttons of the control must use as their text
	 *         color or null if the control buttons must have the default text
	 *         color.
	 */
	ColorSpecification getButtonForegroundColor();

	/**
	 * @return the border color that buttons of the control must use as their border
	 *         color or null if the control buttons must have the default border.
	 */
	ColorSpecification getButtonBorderColor();

	/**
	 * Allows the control to constructs a new instance of the edited control value.
	 * 
	 * @param typeToInstanciate     The type of the value to instanciate.
	 * @param selectableConstructor Whether the framework should allow to select a
	 *                              constructor or not.
	 * @return the new instance.
	 */
	Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor);

}
