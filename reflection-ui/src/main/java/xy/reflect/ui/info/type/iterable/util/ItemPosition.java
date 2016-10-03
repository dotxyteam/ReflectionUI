package xy.reflect.ui.info.type.iterable.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ItemPosition {

	protected IFieldInfo containingListField;
	protected ItemPosition parentItemPosition;
	protected int index;
	protected Object rootListOwner;

	public ItemPosition(IFieldInfo containingListField, ItemPosition parentItemPosition, int index,
			Object rootListOwner) {
		this.containingListField = containingListField;
		this.parentItemPosition = parentItemPosition;
		this.index = index;
		this.rootListOwner = rootListOwner;
	}

	public boolean supportsItem(Object object) {
		ITypeInfo itemType = getContainingListType().getItemType();
		return (itemType == null) || (itemType.supportsInstance(object));
	}

	public boolean isContainingListReadOnly() {
		ItemPosition parent = getParentItemPosition();
		if (parent != null) {
			if (parent.isContainingListReadOnly()) {
				return true;
			}
		}
		IListTypeInfo containingListType = getContainingListType();
		if (!containingListType.canReplaceContent()) {
			if (!(containingListType.canInstanciateFromArray() && !getContainingListField().isGetOnly())) {
				return true;
			}
		}
		return false;
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

	public IFieldInfo getContainingListField() {
		return containingListField;
	}

	public Object[] getContainingListRawValue() {
		Object list = getContainingListField().getValue(getContainingListOwner());
		return getContainingListType().toArray(list);
	}

	public IListTypeInfo getContainingListType() {
		return (IListTypeInfo) getContainingListField().getType();
	}

	public ItemPosition getParentItemPosition() {
		return parentItemPosition;
	}

	@Override
	public String toString() {
		return "Item(depth=" + getDepth() + ", position=" + getIndex() + ", value=" + getItem() + ")";
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

	public String getContainingListPath() {
		String result = this.containingListField.getName();
		ItemPosition parent = getParentItemPosition();
		if (parent == null) {
			result = "<root>/" + result;
		} else {
			result = parent.getContainingListPath() + "/" + result;
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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ItemPosition)) {
			return false;
		}
		ItemPosition other = (ItemPosition) obj;
		if (!ReflectionUIUtils.equalsOrBothNull(getParentItemPosition(), other.getParentItemPosition())) {
			return false;
		}
		if (getIndex() != other.getIndex()) {
			return false;
		}
		return true;
	}

	public Object getContainingListOwner() {
		if (getParentItemPosition() != null) {
			return getParentItemPosition().getItem();
		} else {
			return rootListOwner;
		}

	}

	public ItemPosition getSibling(int index2) {
		return new ItemPosition(getContainingListField(), getParentItemPosition(), index2, rootListOwner);
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

}
