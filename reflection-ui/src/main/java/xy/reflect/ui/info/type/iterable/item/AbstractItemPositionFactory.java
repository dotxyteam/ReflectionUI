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
package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

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
