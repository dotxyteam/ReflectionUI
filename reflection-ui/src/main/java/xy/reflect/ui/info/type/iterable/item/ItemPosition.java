package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class ItemPosition implements Cloneable {

	protected ItemPosition parentItemPosition;
	protected IControlData containingListData;
	protected int index;
	protected Object item;

	public ItemPosition(ItemPosition parentItemPosition, IControlData containingListData, int index) {
		this.parentItemPosition = parentItemPosition;
		this.containingListData = containingListData;
		this.index = index;
		updateItem();
	}

	protected void updateItem() {
		Object[] containingListRawValue = getContainingListRawValue();
		if ((index >= 0) && (index < containingListRawValue.length)) {
			item = containingListRawValue[index];
		} else {
			item = null;
		}
	}

	public boolean supportsItem(Object object) {
		ITypeInfo itemType = getContainingListType().getItemType();
		return (itemType == null) || (itemType.supportsInstance(object));
	}

	public int getIndex() {
		return index;
	}

	public Object getItem() {
		return item;
	}

	public boolean isNullable() {
		ITypeInfo itemType = getContainingListType().getItemType();
		if (itemType == null) {
			return true;
		}
		return itemType.isPassedByReference();
	}

	public String getContainingListTitle() {
		return null;
	}

	public IControlData getContainingListData() {
		return containingListData;
	}

	public Object[] getContainingListRawValue() {
		Object list = getContainingListData().getValue();
		if(list == null){
			return new Object[0];
		}
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
		ItemPosition result = (ItemPosition) clone();
		result.index = index2;
		result.updateItem();
		return result;
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
	public ItemPosition clone() {
		try {
			return (ItemPosition) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new ReflectionUIError(e);
		}
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
		StringBuilder path = new StringBuilder();
		ItemPosition current = this;
		while (current != null) {
			if (current == this) {
				path.insert(0, "Item" + current.index);
			} else {
				path.insert(0, "Item" + current.index + "->Sub");
			}
			current = current.getParentItemPosition();
		}
		return "ItemPosition [item=" + getItem() + ", path=" + path.toString() + "]";
	}

}
