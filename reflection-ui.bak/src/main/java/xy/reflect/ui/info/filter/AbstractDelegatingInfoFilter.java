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
package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public abstract class AbstractDelegatingInfoFilter implements IInfoFilter {

	protected abstract IInfoFilter getDelegate();

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
		result = prime * result + ((getDelegate() == null) ? 0 : getDelegate().hashCode());
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
		AbstractDelegatingInfoFilter other = (AbstractDelegatingInfoFilter) obj;
		if (getDelegate() == null) {
			if (other.getDelegate() != null)
				return false;
		} else if (!getDelegate().equals(other.getDelegate()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingInfoFilter [getDelegate()=" + getDelegate() + "]";
	}

	
}
