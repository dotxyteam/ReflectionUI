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
package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

/**
 * Base class of factories that are intended to create directly or indirectly
 * all {@link ItemPosition} instances. Actually these factories only create root
 * item positions that will create children or sibling item positions.
 * 
 * @author olitank
 *
 */
public abstract class AbstractItemPositionFactory {

	public abstract Object getRootListValue();

	public abstract IListTypeInfo getRootListType();

	public abstract ValueReturnMode getRootListValueReturnMode();

	public abstract boolean isRootListGetOnly();

	public abstract String getRootListTitle();

	public ItemPosition getRootItemPosition(int index) {
		ItemPosition result = createItemPosition();
		result.factory = this;
		result.parentItemPosition = null;
		result.containingListFieldIfNotRoot = null;
		result.index = index;
		return result;
	}

	protected ItemPosition createItemPosition() {
		return new ItemPosition();
	}

	public Object[] retrieveRootListRawValue() {
		Object rootListValue = getRootListValue();
		if (rootListValue == null) {
			return new Object[0];
		}
		return getRootListType().toArray(rootListValue);
	}

}
