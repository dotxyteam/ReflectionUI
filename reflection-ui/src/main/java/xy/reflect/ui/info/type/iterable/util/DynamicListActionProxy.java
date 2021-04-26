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
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * Dynamic list action proxy class. The methods in this class should be
 * overriden to provide custom information.
 * 
 * @author olitank
 *
 */
public class DynamicListActionProxy implements IDynamicListAction {

	protected IDynamicListAction base;

	public DynamicListActionProxy(IDynamicListAction base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
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

	public String getSignature() {
		return base.getSignature();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(Object object, InvocationData invocationData) {
		return base.invoke(object, invocationData);
	}

	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	public InfoCategory getCategory() {
		return base.getCategory();
	}

	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return base.getNextInvocationUndoJob(object, invocationData);
	}

	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		base.validateParameters(object, invocationData);
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	public boolean isNullReturnValueDistinct() {
		return base.isNullReturnValueDistinct();
	}

	public boolean isReturnValueIgnored() {
		return base.isReturnValueIgnored();
	}

	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return base.getConfirmationMessage(object, invocationData);
	}

	public boolean isHidden() {
		return base.isHidden();
	}

	public void onControlVisibilityChange(Object object, boolean b) {
		base.onControlVisibilityChange(object, b);
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
		DynamicListActionProxy other = (DynamicListActionProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ListActionProxy [signature=" + getSignature() + ", base=" + base + "]";
	}

}
