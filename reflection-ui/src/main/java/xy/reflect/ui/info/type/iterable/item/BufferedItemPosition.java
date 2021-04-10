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

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;

public class BufferedItemPosition extends ItemPosition {

	public static final Object NULL_FAKE_ITEM = new Object() {
		@Override
		public String toString() {
			return "NULL_BUFFERED_ITEM_REMEMBERED";
		}

	};
	protected Object fakeItem;
	protected Object[] bufferedSubListRawValue;
	protected IFieldInfo bufferedSubListField;

	protected BufferedItemPosition() {
		super();
	}

	@Override
	public Object getItem() {
		if (fakeItem != null) {
			if (fakeItem == NULL_FAKE_ITEM) {
				return null;
			} else {
				return fakeItem;
			}
		}
		return super.getItem();
	}

	public void setFakeItem(Object item) {
		if (item == null) {
			fakeItem = NULL_FAKE_ITEM;
		} else {
			fakeItem = item;
		}
	}

	public void unsetFakeItem() {
		fakeItem = null;
	}

	public void refreshContainingList() {
		if (isRoot()) {
			((AbstractBufferedItemPositionFactory) factory).refresh();
		} else {
			((BufferedItemPosition) parentItemPosition).bufferedSubListRawValue = null;
			((BufferedItemPosition) parentItemPosition).bufferedSubListField = null;
		}
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		return (BufferedItemPosition) parentItemPosition;
	}

	@Override
	public BufferedItemPosition getRoot() {
		return (BufferedItemPosition) super.getRoot();
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSibling(index2);
		result.fakeItem = null;
		return result;
	}

	@Override
	public List<BufferedItemPosition> getPreviousSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getPreviousSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getFollowingSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getFollowingSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public IFieldInfo getSubListField() {
		if (bufferedSubListField == null) {
			bufferedSubListField = super.getSubListField();
		}
		return bufferedSubListField;
	}

	@Override
	public Object[] retrieveSubListRawValue() {
		if (bufferedSubListRawValue == null) {
			bufferedSubListRawValue = super.retrieveSubListRawValue();
		}
		return bufferedSubListRawValue;
	}

	@Override
	public Object updateContainingList(Object[] newContainingListRawValue) {
		Object result = super.updateContainingList(newContainingListRawValue);
		refreshContainingList();
		return result;
	}

	@Override
	public BufferedItemPosition getSubItemPosition(int index) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSubItemPosition(index);
		if (result != null) {
			result.bufferedSubListField = null;
			result.bufferedSubListRawValue = null;
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getSubItemPositions() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getSubItemPositions()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fakeItem == null) ? 0 : fakeItem.hashCode());
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
		BufferedItemPosition other = (BufferedItemPosition) obj;
		if (fakeItem == null) {
			if (other.fakeItem != null)
				return false;
		} else if (!fakeItem.equals(other.fakeItem))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}
