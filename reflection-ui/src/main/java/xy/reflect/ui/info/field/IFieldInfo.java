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

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented field properties.
 * 
 * @author olitank
 *
 */
public interface IFieldInfo extends IInfo {

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 */
	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		ITypeInfo type = new DefaultTypeInfo(ReflectionUIUtils.STANDARD_REFLECTION,
				new JavaTypeInfoSource(Object.class, null));

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean visible) {
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
		public boolean isHidden() {
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
		public void setValue(Object object, Object value) {
		}

		@Override
		public boolean isGetOnly() {
			return true;
		}

		@Override
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public String getNullValueLabel() {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return type;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
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
		public String toString() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return -1;
		}

	};

	/**
	 * @return UI-oriented type properties of the current field.
	 */
	ITypeInfo getType();

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @return the value of this field extracted from the given object.
	 */
	Object getValue(Object object);

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @return options for value of this field or null if there is not any know
	 *         option.
	 */
	Object[] getValueOptions(Object object);

	/**
	 * Updates the current field of the given object with the given value.
	 * 
	 * @param object
	 *            The object hosting the field value.
	 * @param value
	 *            The new field value.
	 */
	void setValue(Object object, Object value);

	/**
	 * @param object
	 *            The object hosting the field value.
	 * @param newValue
	 *            The new field value.
	 * @return a job that can revert the next field value update or null if the
	 *         default undo job should be used.
	 */
	Runnable getNextUpdateCustomUndoJob(Object object, Object newValue);

	/**
	 * @return true if and only if this field control must distinctly display and
	 *         allow to set the null value. This is usually needed if a null value
	 *         has a special meaning different from "empty/default value" for the
	 *         developer.
	 */
	boolean isNullValueDistinct();

	/**
	 * @return true if and only if this field value can be updated. Then
	 *         {@link #setValue(Object, Object)} should not be called.
	 */
	boolean isGetOnly();

	/**
	 * @return a text that should be displayed by the field control to describe the
	 *         null value.
	 */
	String getNullValueLabel();

	/**
	 * @return the value return mode of this field. It may impact the behavior of
	 *         this field control.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the category in which this field will be displayed.
	 */
	InfoCategory getCategory();

	/**
	 * @return true if this field value is forcibly displayed as a generic form. If
	 *         false is returned then a custom control may be displayed. Note that
	 *         the form is either embedded in the parent form or displayed in a
	 *         child dialog according to the return value of
	 *         {@link #isFormControlEmbedded()}.
	 */
	boolean isFormControlMandatory();

	/**
	 * @return whether this field value form is embedded in the parent form or
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
	 * @return the automatic update period (in milliseconds) that this field control
	 *         will try to respect.-1 means that there is no automatic update and 0
	 *         means that the update occurs as fast as possible.
	 */
	long getAutoUpdatePeriodMilliseconds();

	/**
	 * @return true if and only if this field control is filtered out from the
	 *         display.
	 */
	boolean isHidden();

	/**
	 * @return a number that specifies how to distribute extra horizontal space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         horizontally than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its horizontal weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 */
	double getDisplayAreaHorizontalWeight();

	/**
	 * @return a number that specifies how to distribute extra vertical space
	 *         between sibling field controls. If the resulting layout is smaller
	 *         vertically than the area it needs to fill, the extra space is
	 *         distributed to each field in proportion to its vertical weight. A
	 *         field that has a weight of zero receives no extra space. If all the
	 *         weights are zero, all the extra space appears between the grids of
	 *         the cell and the left and right edges. It should be a non-negative
	 *         value.
	 * 
	 */
	double getDisplayAreaVerticalWeight();

	/**
	 * This method should be called by the renderer when the visibility of this
	 * field changes for the given object in the generated UI.
	 * 
	 * @param object
	 *            The object hosting the field value.
	 * @param visible
	 *            true when the field becomes visible, false when it becomes
	 *            invisible.
	 */
	void onControlVisibilityChange(Object object, boolean visible);

}
