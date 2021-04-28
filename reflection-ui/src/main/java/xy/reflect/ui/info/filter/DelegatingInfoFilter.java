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
package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Filter that delegates to another filter that can be replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingInfoFilter implements IInfoFilter {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IInfoFilter getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

	public boolean excludeField(IFieldInfo field) {
		return getDelegate().excludeField(field);
	}

	public boolean excludeMethod(IMethodInfo method) {
		return getDelegate().excludeMethod(method);
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
		DelegatingInfoFilter other = (DelegatingInfoFilter) obj;
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingInfoFilter [delegate=" + getDelegateId() + "]";
	}

}
