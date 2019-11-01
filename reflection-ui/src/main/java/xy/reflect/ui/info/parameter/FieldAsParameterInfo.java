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
package xy.reflect.ui.info.parameter;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;

public class FieldAsParameterInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo field;
	protected int position;
	protected ITypeInfo type;

	public FieldAsParameterInfo(ReflectionUI reflectionUI, IFieldInfo field, int position) {
		this.reflectionUI = reflectionUI;
		this.field = field;
		this.position = position;
	}

	public IFieldInfo getSourceField() {
		return field;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public String getOnlineHelp() {
		return field.getOnlineHelp();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getCaption() {
		return field.getCaption();
	}

	@Override
	public boolean isNullValueDistinct() {
		return field.isNullValueDistinct();
	}

	@Override
	public boolean isHidden() {
		return field.isHidden();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new TypeInfoSourceProxy(field.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return null;
				}
			});
		}
		return type;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public Object getDefaultValue(Object object) {
		return field.getValue(object);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return field.hasValueOptions(object);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return field.getValueOptions(object);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + position;
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
		FieldAsParameterInfo other = (FieldAsParameterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsParameterInfo [field=" + field + ", position=" + position + "]";
	}

}
