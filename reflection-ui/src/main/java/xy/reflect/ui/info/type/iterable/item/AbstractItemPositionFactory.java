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
 * Base class of factories that create directly or indirectly all
 * {@link ItemPosition} instances. Actually these factories only create root
 * item positions that will generate children or sibling item positions.
 * 
 * @author olitank
 *
 */
public abstract class AbstractItemPositionFactory {

	/**
	 * @return the root list value.
	 */
	public abstract Object retrieveRootListValue();

	/**
	 * Updates the root list value.
	 * 
	 * @param rootListValue The new root list value.
	 */
	public abstract void commitRootListValue(Object rootListValue);

	/**
	 * @return the type information of the root list value.
	 */
	public abstract IListTypeInfo getRootListType();

	/**
	 * @return root list value return mode.
	 */
	public abstract ValueReturnMode getRootListValueReturnMode();

	/**
	 * @return false if and only if the root list value can be set. Otherwise
	 *         {@link #commitRootListValue(Object)} should not be called.
	 */
	public abstract boolean isRootListGetOnly();

	/**
	 * @return the display name of the root list.
	 */
	public abstract String getRootListTitle();

	/**
	 * @param index
	 * @return a new root item position initialized with given index.
	 */
	public ItemPosition getRootItemPosition(int index) {
		ItemPosition result = createItemPosition();
		result.factory = this;
		result.parentItemPosition = null;
		result.containingListFieldIfNotRoot = null;
		result.index = index;
		return result;
	}

	/**
	 * @return an array containing the root list items.
	 */
	public Object[] retrieveRootListRawValue() {
		Object rootListValue = retrieveRootListValue();
		if (rootListValue == null) {
			return new Object[0];
		}
		return getRootListType().toArray(rootListValue);
	}

	protected ItemPosition createItemPosition() {
		return new ItemPosition();
	}

}
