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

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public class DefaultFieldControlData implements IFieldControlData {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	public DefaultFieldControlData(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
	}

	public DefaultFieldControlData(ReflectionUI reflectionUI) {
		this(reflectionUI, null, IFieldInfo.NULL_FIELD_INFO);
	}

	@Override
	public Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor) {
		throw new UnsupportedOperationException();
	}

	public Object getObject() {
		return object;
	}

	public IFieldInfo getField() {
		return field;
	}

	@Override
	public Object getValue() {
		return getField().getValue(getObject());
	}

	@Override
	public void setValue(Object value) {
		getField().setValue(getObject(), value);
	}

	@Override
	public String getCaption() {
		return getField().getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return getField().getOnlineHelp();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return getField().getNextUpdateCustomUndoJob(getObject(), newValue);
	}

	@Override
	public ITypeInfo getType() {
		return getField().getType();
	}

	@Override
	public boolean isGetOnly() {
		return getField().isGetOnly();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return getField().getValueReturnMode();
	}

	@Override
	public boolean isNullValueDistinct() {
		return getField().isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return getField().getNullValueLabel();
	}

	public boolean isFormControlMandatory() {
		return getField().isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return getField().isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return getField().getFormControlFilter();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return getField().getSpecificProperties();
	}

	@Override
	public ColorSpecification getLabelForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormForegroundColor() != null) {
				return type.getFormForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainForegroundColor();
	}

	@Override
	public ColorSpecification getBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormBorderColor() != null) {
				return type.getFormBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainBorderColor();
	}

	@Override
	public ColorSpecification getEditorForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsForegroundColor() != null) {
				return type.getFormEditorsForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorForegroundColor();
	}

	@Override
	public ColorSpecification getEditorBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsBackgroundColor() != null) {
				return type.getFormEditorsBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorBackgroundColor();
	}

	@Override
	public ResourcePath getButtonBackgroundImagePath() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundImagePath() != null) {
				return type.getFormButtonBackgroundImagePath();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath();
	}

	@Override
	public ColorSpecification getButtonBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundColor() != null) {
				return type.getFormButtonBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundColor();
	}

	@Override
	public ColorSpecification getButtonForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonForegroundColor() != null) {
				return type.getFormButtonForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonForegroundColor();
	}

	@Override
	public ColorSpecification getButtonBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBorderColor() != null) {
				return type.getFormButtonBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBorderColor();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getField() == null) ? 0 : getField().hashCode());
		result = prime * result + ((getObject() == null) ? 0 : getObject().hashCode());
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
		DefaultFieldControlData other = (DefaultFieldControlData) obj;
		if (getField() == null) {
			if (other.getField() != null)
				return false;
		} else if (!getField().equals(other.getField()))
			return false;
		if (getObject() == null) {
			if (other.getObject() != null)
				return false;
		} else if (!getObject().equals(other.getObject()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlData [object=" + getObject() + ", field=" + getField() + "]";
	}

}
