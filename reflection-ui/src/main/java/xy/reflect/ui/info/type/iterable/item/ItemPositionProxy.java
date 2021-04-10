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

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;

public class ItemPositionProxy extends ItemPosition {

	protected ItemPosition base;

	public ItemPositionProxy(ItemPosition base) {
		this.base = base;
	}

	public ItemPosition getBase() {
		return base;
	}

	public void setBase(ItemPosition base) {
		this.base = base;
	}

	public boolean supportsItem(Object object) {
		return base.supportsItem(object);
	}

	public int getIndex() {
		return base.getIndex();
	}

	public Object getItem() {
		return base.getItem();
	}

	public String getContainingListTitle() {
		return base.getContainingListTitle();
	}

	public IListTypeInfo getContainingListType() {
		return base.getContainingListType();
	}

	public ItemPosition getParentItemPosition() {
		return base.getParentItemPosition();
	}

	public String getPath() {
		return base.getPath();
	}

	public int getDepth() {
		return base.getDepth();
	}

	public List<? extends ItemPosition> getPreviousSiblings() {
		return base.getPreviousSiblings();
	}

	public List<? extends ItemPosition> getFollowingSiblings() {
		return base.getFollowingSiblings();
	}

	public List<ItemPosition> getAncestors() {
		return base.getAncestors();
	}

	public ItemPosition getSibling(int index2) {
		return base.getSibling(index2);
	}

	public IFieldInfo getContainingListFieldIfNotRoot() {
		return base.getContainingListFieldIfNotRoot();
	}

	public int getContainingListSize() {
		return base.getContainingListSize();
	}

	public ValueReturnMode geContainingListReturnMode() {
		return base.geContainingListReturnMode();
	}

	public boolean isContainingListGetOnly() {
		return base.isContainingListGetOnly();
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		return base.getSubItemPositions();
	}

	public boolean isRoot() {
		return base.isRoot();
	}

	public ItemPosition getRoot() {
		return base.getRoot();
	}

	public ValueReturnMode getItemReturnMode() {
		return base.getItemReturnMode();
	}

	public Object[] retrieveSubListRawValue() {
		return base.retrieveSubListRawValue();
	}

	public IFieldInfo getSubListField() {
		return base.getSubListField();
	}

	public ItemPosition getSubItemPosition(int index) {
		return base.getSubItemPosition(index);
	}

	public IListStructuralInfo getContainingListStructuralInfo() {
		return base.getContainingListStructuralInfo();
	}

	public ItemPosition clone() {
		return base.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemPositionProxy other = (ItemPositionProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ItemPositionProxy [delegate=" + base + "]";
	}

}
