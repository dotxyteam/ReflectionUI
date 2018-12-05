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

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldControlData {

	Object getValue();

	void setValue(Object value);

	String getCaption();

	Runnable getNextUpdateCustomUndoJob(Object newValue);

	ITypeInfo getType();

	boolean isGetOnly();

	ValueReturnMode getValueReturnMode();

	boolean isNullValueDistinct();

	String getNullValueLabel();

	boolean isFormControlMandatory();

	boolean isFormControlEmbedded();

	IInfoFilter getFormControlFilter();

	Map<String, Object> getSpecificProperties();

	ColorSpecification getForegroundColor();

	ColorSpecification getBorderColor();

	Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor);

}
