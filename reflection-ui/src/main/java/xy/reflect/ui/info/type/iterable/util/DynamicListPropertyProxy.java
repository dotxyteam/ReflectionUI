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
package xy.reflect.ui.info.type.iterable.util;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * Dynamic list property proxy class. The methods in this class should be
 * overriden to provide custom information.
 * 
 * @author olitank
 *
 */
public class DynamicListPropertyProxy implements IDynamicListProperty {

	protected IDynamicListProperty base;

	public DynamicListPropertyProxy(IDynamicListProperty base) {
		super();
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public void onControlVisibilityChange(Object object, boolean visible) {
		base.onControlVisibilityChange(object, visible);
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public List<ItemPosition> getPostSelection() {
		return base.getPostSelection();
	}

	public boolean isEnabled() {
		return base.isEnabled();
	}

	public ITypeInfo getType() {
		return base.getType();
	}

	

	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return base.getAlternativeConstructors(object);
	}

	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return base.getAlternativeListItemConstructors(object);
	}

	public Object getValue(Object object) {
		return base.getValue(object);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return base.hasValueOptions(object);
	}

	public Object[] getValueOptions(Object object) {
		return base.getValueOptions(object);
	}

	public void setValue(Object object, Object value) {
		base.setValue(object, value);
	}

	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return base.getNextUpdateCustomUndoJob(object, newValue);
	}

	public boolean isNullValueDistinct() {
		return base.isNullValueDistinct();
	}

	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	public boolean isTransient() {
		return base.isTransient();
	}

	public String getNullValueLabel() {
		return base.getNullValueLabel();
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public InfoCategory getCategory() {
		return base.getCategory();
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

	public long getAutoUpdatePeriodMilliseconds() {
		return base.getAutoUpdatePeriodMilliseconds();
	}

	public boolean isHidden() {
		return base.isHidden();
	}

	public double getDisplayAreaHorizontalWeight() {
		return base.getDisplayAreaHorizontalWeight();
	}

	public double getDisplayAreaVerticalWeight() {
		return base.getDisplayAreaVerticalWeight();
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
		DynamicListPropertyProxy other = (DynamicListPropertyProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ListPropertyProxy [name=" + getName() + ", base=" + base + "]";
	}

}
