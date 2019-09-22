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
package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class ListModificationFactory {

	protected ItemPosition anyItemPosition;
	protected Mapper<Object, IModification> rootListValueCommitModificationAccessor;

	public ListModificationFactory(ItemPosition anyListItemPosition,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		this.anyItemPosition = anyListItemPosition;
		this.rootListValueCommitModificationAccessor = rootListValueCommitModificationAccessor;
	}

	public IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue) {
		return new ListModification(itemPosition, newListRawValue, rootListValueCommitModificationAccessor);
	}

	public boolean canAdd(int index) {
		if ((index < 0) || (index > anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	public boolean canAddAll(int index, List<Object> items) {
		if (!canAdd(index)) {
			return false;
		}
		ITypeInfo itemType = anyItemPosition.getContainingListType().getItemType();
		if (itemType != null) {
			for (Object item : items) {
				if (!itemType.supportsInstance(item)) {
					return false;
				}
			}
		}
		return true;
	}

	public IModification add(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue);
	}

	public boolean canRemove(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	public IModification remove(int index) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.remove(index);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue);
	}

	public boolean canSet(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	public boolean canSet(int index, Object item) {
		if (!canSet(index)) {
			return false;
		}
		ITypeInfo itemType = anyItemPosition.getContainingListType().getItemType();
		if (itemType != null) {
			if (!itemType.supportsInstance(item)) {
				return false;
			}
		}
		return true;
	}

	public IModification set(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.set(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue);
	}

	public boolean canMove(int index, int offset) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		int index2 = index + offset;
		if ((index2 < 0) || (index2 >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	public IModification move(int index, int offset) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index + offset, tmpList.remove(index));
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue);
	}

	public boolean canClear() {
		return anyItemPosition.isContainingListEditable();
	}

	public IModification clear() {
		return createListModification(anyItemPosition, new Object[0]);
	}

	protected static class ListModification implements IModification {

		protected ItemPosition itemPosition;
		protected Object[] newListRawValue;
		protected Mapper<Object, IModification> rootListValueCommitModificationAccessor;

		public ListModification(ItemPosition itemPosition, Object[] newListRawValue,
				Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			this.itemPosition = itemPosition;
			this.newListRawValue = newListRawValue;
			this.rootListValueCommitModificationAccessor = rootListValueCommitModificationAccessor;
		}

		@Override
		public String getTitle() {
			return FieldControlDataModification.getTitle(itemPosition.getContainingListTitle());
		}

		@Override
		public IModification applyAndGetOpposite() {
			Object[] oldListRawValue = itemPosition.retrieveContainingListRawValue();
			Object newRootListValue = itemPosition.updateContainingList(newListRawValue);
			if (newRootListValue == null) {
				newRootListValue = itemPosition.getFactory().getRootListValue();
			}
			rootListValueCommitModificationAccessor.get(newRootListValue).applyAndGetOpposite();
			return new ListModification(itemPosition, oldListRawValue, rootListValueCommitModificationAccessor);
		}

		@Override
		public boolean isNull() {
			return false;
		}

	}

}
