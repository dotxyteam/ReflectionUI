package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class ItemPosition implements Cloneable {

	protected ItemPosition parentItemPosition;
	protected IFieldControlData containingListDataIfRoot;
	protected int index;
	protected IFieldInfo containingListFieldIfNotRoot;
	protected int containingListSize;

	protected ItemPosition(ItemPosition parentItemPosition, IFieldControlData containingListDataIfRoot,
			IFieldInfo containingListFieldIfNotRoot, int containingListSize, int index) {
		if ((containingListFieldIfNotRoot != null) && (containingListDataIfRoot != null)) {
			throw new ReflectionUIError();
		}
		this.parentItemPosition = parentItemPosition;
		this.containingListDataIfRoot = containingListDataIfRoot;
		this.containingListFieldIfNotRoot = containingListFieldIfNotRoot;
		this.containingListSize = containingListSize;
		this.index = index;
	}

	public ItemPosition(IFieldControlData containingListDataIfRoot, int index) {
		this(null, containingListDataIfRoot, null, -1, index);
		this.containingListSize = retrieveContainingListRawValue().length;
	}

	public IFieldControlData getContainingListDataIfRoot() {
		return containingListDataIfRoot;
	}

	public IFieldInfo getContainingListFieldIfNotRoot() {
		return containingListFieldIfNotRoot;
	}

	public int getContainingListSize() {
		return containingListSize;
	}

	public ItemPosition getParentItemPosition() {
		return parentItemPosition;
	}

	public int getIndex() {
		return index;
	}

	public boolean supportsItem(Object object) {
		ITypeInfo itemType = getContainingListType().getItemType();
		return (itemType == null) || (itemType.supportsInstance(object));
	}

	public IListTypeInfo getContainingListType() {
		if (isRoot()) {
			return (IListTypeInfo) containingListDataIfRoot.getType();
		} else {
			return (IListTypeInfo) containingListFieldIfNotRoot.getType();
		}
	}

	public String getContainingListTitle() {
		if (isRoot()) {
			return containingListDataIfRoot.getCaption();
		} else {
			return containingListFieldIfNotRoot.getCaption();
		}
	}

	public Object getItem() {
		Object[] containingListRawValue = retrieveContainingListRawValue();
		if ((index >= 0) && (index < containingListRawValue.length)) {
			return containingListRawValue[index];
		} else {
			return null;
		}
	}

	public Object retrieveContainingListValue() {
		if (isRoot()) {
			return containingListDataIfRoot.getValue();
		} else {
			return containingListFieldIfNotRoot.getValue(parentItemPosition.getItem());
		}
	}

	public Object[] retrieveContainingListRawValue() {
		Object list = retrieveContainingListValue();
		if (list == null) {
			return new Object[0];
		}
		return getContainingListType().toArray(list);
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

	public List<? extends ItemPosition> getPreviousSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < getIndex(); i++) {
			result.add(getSibling(i));
		}
		Collections.reverse(result);
		return result;
	}

	public List<? extends ItemPosition> getFollowingSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = getIndex() + 1; i < retrieveContainingListRawValue().length; i++) {
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
		return result;
	}

	public ItemPosition getAnySubItemPosition() {
		IListStructuralInfo treeInfo = getRootListItemPosition().getContainingListType().getStructuralInfo();
		if (treeInfo == null) {
			return null;
		}
		final Object item = getItem();
		IFieldInfo subListField = treeInfo.getItemSubListField(new DelegatingItemPosition(this) {
			@Override
			public Object getItem() {
				return item;
			}
		});
		if (subListField == null) {
			return null;
		}
		ItemPosition result = clone();
		result.parentItemPosition = this;
		result.containingListDataIfRoot = null;
		result.containingListFieldIfNotRoot = subListField;
		{
			Object list = subListField.getValue(item);
			if (list == null) {
				result.containingListSize = 0;
			}
			result.containingListSize = result.getContainingListType().toArray(list).length;
		}
		result.index = -1;
		return result;
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		ItemPosition anySubItemPosition = getAnySubItemPosition();
		if (anySubItemPosition == null) {
			return Collections.emptyList();
		}
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < anySubItemPosition.retrieveContainingListRawValue().length; i++) {
			result.add(anySubItemPosition.getSibling(i));
		}
		return result;
	}

	public boolean isRoot() {
		return parentItemPosition == null;
	}

	public ItemPosition getRootListItemPosition() {
		ItemPosition current = this;
		while (!current.isRoot()) {
			current = current.getParentItemPosition();
		}
		return current;
	}

	public IFieldControlData getRootListData() {
		return getRootListItemPosition().containingListDataIfRoot;
	}

	public ValueReturnMode getItemReturnMode() {
		ValueReturnMode result = ValueReturnMode.combine(geContainingListReturnMode(),
				getContainingListType().getItemReturnMode());
		if (parentItemPosition != null) {
			result = ValueReturnMode.combine(parentItemPosition.getItemReturnMode(), result);
		}
		return result;
	}

	public ValueReturnMode geContainingListReturnMode() {
		if (isRoot()) {
			return containingListDataIfRoot.getValueReturnMode();
		} else {
			return containingListFieldIfNotRoot.getValueReturnMode();
		}
	}

	public boolean isContainingListGetOnly() {
		if (isRoot()) {
			return containingListDataIfRoot.isGetOnly();
		} else {
			return containingListFieldIfNotRoot.isGetOnly();
		}
	}

	public String getPath() {
		StringBuilder result = new StringBuilder();
		ItemPosition current = this;
		while (current != null) {
			if (current == this) {
				result.insert(0, "Item" + current.index);
			} else {
				result.insert(0, "Item" + current.index + "->Sub");
			}
			current = current.getParentItemPosition();
		}
		return result.toString();
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
		result = prime * result + ((containingListDataIfRoot == null) ? 0 : containingListDataIfRoot.hashCode());
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
		if (containingListDataIfRoot == null) {
			if (other.containingListDataIfRoot != null)
				return false;
		} else if (!containingListDataIfRoot.equals(other.containingListDataIfRoot))
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
		return "ItemPosition [item=" + getItem() + ", path=" + getPath() + "]";
	}

}
