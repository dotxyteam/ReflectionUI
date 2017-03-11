package xy.reflect.ui.info.type.iterable.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public abstract class ItemPosition implements Cloneable {

	protected ItemPosition parentItemPosition;
	protected IFieldInfo containingListField;
	protected int index;

	public abstract Object getRootListOwner();

	public ItemPosition(ItemPosition parentItemPosition, IFieldInfo containingListField, int index) {
		this.parentItemPosition = parentItemPosition;
		this.containingListField = containingListField;
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

	public IFieldInfo getContainingListField() {
		return containingListField;
	}

	public Object[] getContainingListRawValue() {
		Object containingListOwner;
		if (parentItemPosition != null) {
			containingListOwner = parentItemPosition.getItem();
		} else {
			containingListOwner = getRootListOwner();
		}		
		Object list = getContainingListField().getValue(containingListOwner);
		if (list == null) {
			return new Object[0];
		}
		return getContainingListType().toArray(list);
	}

	public IListTypeInfo getContainingListType() {
		return (IListTypeInfo) getContainingListField().getType();
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
		ItemPosition result;
		try {
			result = (ItemPosition) clone();
		} catch (CloneNotSupportedException e) {
			throw new ReflectionUIError(e);
		}
		result.index = index2;
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
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingListField == null) ? 0 : containingListField.hashCode());
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
		if (containingListField == null) {
			if (other.containingListField != null)
				return false;
		} else if (!containingListField.equals(other.containingListField))
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
