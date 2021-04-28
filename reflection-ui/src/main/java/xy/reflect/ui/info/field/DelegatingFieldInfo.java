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

import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Field proxy that delegates to another field that can be replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingFieldInfo implements IFieldInfo {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IFieldInfo getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

	public String getName() {
		return getDelegate().getName();
	}

	public String getCaption() {
		return getDelegate().getCaption();
	}

	public String getOnlineHelp() {
		return getDelegate().getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return getDelegate().getSpecificProperties();
	}

	public ITypeInfo getType() {
		return getDelegate().getType();
	}

	public Object getValue(Object object) {
		return getDelegate().getValue(object);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return getDelegate().hasValueOptions(object);
	}

	public Object[] getValueOptions(Object object) {
		return getDelegate().getValueOptions(object);
	}

	public void setValue(Object object, Object value) {
		getDelegate().setValue(object, value);
	}

	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return getDelegate().getNextUpdateCustomUndoJob(object, newValue);
	}

	public boolean isNullValueDistinct() {
		return getDelegate().isNullValueDistinct();
	}

	public boolean isGetOnly() {
		return getDelegate().isGetOnly();
	}

	@Override
	public boolean isTransient() {
		return getDelegate().isTransient();
	}

	public String getNullValueLabel() {
		return getDelegate().getNullValueLabel();
	}

	public ValueReturnMode getValueReturnMode() {
		return getDelegate().getValueReturnMode();
	}

	public InfoCategory getCategory() {
		return getDelegate().getCategory();
	}

	public boolean isFormControlMandatory() {
		return getDelegate().isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return getDelegate().isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return getDelegate().getFormControlFilter();
	}

	public long getAutoUpdatePeriodMilliseconds() {
		return getDelegate().getAutoUpdatePeriodMilliseconds();
	}

	public boolean isHidden() {
		return getDelegate().isHidden();
	}

	public double getDisplayAreaHorizontalWeight() {
		return getDelegate().getDisplayAreaHorizontalWeight();
	}

	public double getDisplayAreaVerticalWeight() {
		return getDelegate().getDisplayAreaVerticalWeight();
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
		getDelegate().onControlVisibilityChange(object, visible);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDelegateId() == null) ? 0 : getDelegateId().hashCode());
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
		DelegatingFieldInfo other = (DelegatingFieldInfo) obj;
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingFieldInfo [delegate=" + getDelegateId() + "]";
	}

}
