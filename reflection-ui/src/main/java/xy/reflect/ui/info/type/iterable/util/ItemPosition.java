package xy.reflect.ui.info.type.iterable.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

public class ItemPosition {

	protected ItemPosition parentItemPosition;
	protected IControlData containingListData;
	protected int index;

	public ItemPosition(ItemPosition parentItemPosition, IControlData containingListData, int index) {
		this.parentItemPosition = parentItemPosition;
		this.containingListData = containingListData;
		this.index = index;
	}

	public boolean supportsItem(Object object) {
		ITypeInfo itemType = getContainingListType().getItemType();
		return (itemType == null) || (itemType.supportsInstance(object));
	}

	public int getIndex() {
		return index;
	}

	public Object getItem() {
		Object[] listValue = getContainingListRawValue();
		if (index < 0) {
			return null;
		}
		if (index >= listValue.length) {
			return null;
		}
		return listValue[index];
	}

	public String getContainingListCaption() {
		return null;
	}

	public IControlData getContainingListData() {
		return containingListData;
	}

	public Object[] getContainingListRawValue() {
		Object list = getContainingListData().getValue();
		return getContainingListType().toArray(list);
	}

	public IListTypeInfo getContainingListType() {
		return (IListTypeInfo) getContainingListData().getType();
	}

	public ItemPosition getParentItemPosition() {
		return parentItemPosition;
	}

	public int getDepth() {
		int result = 0;
		ItemPosition current = this;
		while (current.getParentItemPosition() != null) {
			current = current.getParentItemPosition();
			result++;
		}
		return result;
	}

	public List<ItemPosition> getPreviousSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < getIndex(); i++) {
			result.add(getSibling(i));
		}
		Collections.reverse(result);
		return result;
	}

	public List<ItemPosition> getFollowingSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = getIndex() + 1; i < getContainingListRawValue().length; i++) {
			result.add(getSibling(i));
		}
		return result;
	}

	public List<ItemPosition> getAncestors() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		ItemPosition ancestor = getParentItemPosition();
		while (ancestor != null) {
			result.add(ancestor);
			ancestor = ancestor.getParentItemPosition();
		}
		return result;
	}

	public ItemPosition getSibling(int index2) {
		return new ItemPosition(parentItemPosition, containingListData, index2);
	}

	public boolean isRootListItemPosition() {
		return getRootListItemPosition().equals(this);
	}

	public ItemPosition getRootListItemPosition() {
		ItemPosition current = this;
		while (current.getParentItemPosition() != null) {
			current = current.getParentItemPosition();
		}
		return current;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingListData == null) ? 0 : containingListData.hashCode());
		result = prime * result + index;
		result = prime * result + ((parentItemPosition == null) ? 0 : parentItemPosition.hashCode());
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
		ItemPosition other = (ItemPosition) obj;
		if (containingListData == null) {
			if (other.containingListData != null)
				return false;
		} else if (!containingListData.equals(other.containingListData))
			return false;
		if (index != other.index)
			return false;
		if (parentItemPosition == null) {
			if (other.parentItemPosition != null)
				return false;
		} else if (!parentItemPosition.equals(other.parentItemPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Item(depth=" + getDepth() + ", position=" + getIndex() + ", value=" + getItem() + ")";
	}

}
