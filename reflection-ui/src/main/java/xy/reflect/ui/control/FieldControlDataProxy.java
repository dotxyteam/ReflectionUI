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
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public class FieldControlDataProxy implements IFieldControlData {

	protected IFieldControlData base;

	public FieldControlDataProxy(IFieldControlData base) {
		super();
		this.base = base;
	}

	public IFieldControlData getBase() {
		return base;
	}

	public Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor) {
		return base.createValue(typeToInstanciate, selectableConstructor);
	}

	public Object getValue() {
		return base.getValue();
	}

	public void setValue(Object value) {
		base.setValue(value);
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return base.getNextUpdateCustomUndoJob(newValue);
	}

	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	public boolean isNullValueDistinct() {
		return base.isNullValueDistinct();
	}

	public String getNullValueLabel() {
		return base.getNullValueLabel();
	}

	public ITypeInfo getType() {
		return base.getType();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public boolean isFormControlMandatory() {
		return base.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return base.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return base.getFormControlFilter();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public ColorSpecification getLabelForegroundColor() {
		return base.getLabelForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return base.getBorderColor();
	}

	public ColorSpecification getEditorBackgroundColor() {
		return base.getEditorBackgroundColor();
	}

	public ColorSpecification getEditorForegroundColor() {
		return base.getEditorForegroundColor();
	}

	public ResourcePath getButtonBackgroundImagePath() {
		return base.getButtonBackgroundImagePath();
	}

	public ColorSpecification getButtonBackgroundColor() {
		return base.getButtonBackgroundColor();
	}

	public ColorSpecification getButtonForegroundColor() {
		return base.getButtonForegroundColor();
	}

	public ColorSpecification getButtonBorderColor() {
		return base.getButtonBorderColor();
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
		FieldControlDataProxy other = (FieldControlDataProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlDataProxy [base=" + base + "]";
	}

}
