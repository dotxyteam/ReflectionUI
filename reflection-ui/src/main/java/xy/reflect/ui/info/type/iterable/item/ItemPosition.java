package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class ItemPosition implements Cloneable {

	protected ItemPositionFactory factory;
	protected ItemPosition parentItemPosition;
	protected int index;
	protected IFieldInfo containingListFieldIfNotRoot;
	protected int containingListSize;

	protected ItemPosition() {
		super();
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
			return factory.getRootListType();
		} else {
			return (IListTypeInfo) containingListFieldIfNotRoot.getType();
		}
	}

	public String getContainingListTitle() {
		if (isRoot()) {
			return factory.getRootListData().getCaption();
		} else {
			return containingListFieldIfNotRoot.getCaption();
		}
	}

	public Object getItem() {
		Object[] containingListRawValue;
		if (isRoot()) {
			containingListRawValue = factory.retrieveRootListRawValue();
		} else {
			containingListRawValue = parentItemPosition.retrieveSubListRawValue();
		}
		if ((index >= 0) && (index < containingListRawValue.length)) {
			return containingListRawValue[index];
		} else {
			return null;
		}
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
		for (int i = getIndex() + 1; i < containingListSize; i++) {
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

	public Object retrieveContainingListValue() {
		if (isRoot()) {
			return factory.retrieveRootListValue();
		} else {
			return parentItemPosition.retrieveSubListValue();
		}
	}
	
	public Object[] retrieveContainingListRawValue() {
		if (isRoot()) {
			return factory.retrieveRootListRawValue();
		} else {
			return parentItemPosition.retrieveSubListRawValue();
		}
	}

	public Object[] retrieveSubListRawValue() {
		Object subListValue = retrieveSubListValue();
		if (subListValue == null) {
			return null;
		} else {
			IListTypeInfo subListType = (IListTypeInfo) getSubListField().getType();
			return subListType.toArray(subListValue);
		}
	}

	public Object retrieveSubListValue() {
		IFieldInfo subListField = getSubListField();
		if (subListField == null) {
			return null;
		}
		final Object item = getItem();
		return subListField.getValue(item);		
	}

	public IFieldInfo getSubListField() {
		IListStructuralInfo treeInfo = getStructuralInfo();
		if (treeInfo == null) {
			return null;
		}
		final Object item = getItem();
		return treeInfo.getItemSubListField(new DelegatingItemPosition(this) {
			@Override
			public Object getItem() {
				return item;
			}
		});
	}

	public ItemPosition getSubItemPosition(int index) {
		Object[] subListRawValue = retrieveSubListRawValue();
		if (subListRawValue == null) {
			return null;
		}
		ItemPosition result = clone();
		result.parentItemPosition = this;
		result.containingListFieldIfNotRoot = getSubListField();
		result.index = index;
		result.containingListSize = subListRawValue.length;
		return result;
	}

	public IListStructuralInfo getStructuralInfo() {
		return getRoot().getContainingListType().getStructuralInfo();
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		Object[] subListRawValue = retrieveSubListRawValue();
		if (subListRawValue == null) {
			return Collections.emptyList();
		}
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int index = 0; index < subListRawValue.length; index++) {
			result.add(getSubItemPosition(index));

		}
		return result;
	}

	public boolean isRoot() {
		return parentItemPosition == null;
	}

	public ItemPosition getRoot() {
		ItemPosition current = this;
		while (!current.isRoot()) {
			current = current.getParentItemPosition();
		}
		return current;
	}

	public IFieldControlData getRootListData() {
		return factory.getRootListData();
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
			return factory.getRootListData().getValueReturnMode();
		} else {
			return containingListFieldIfNotRoot.getValueReturnMode();
		}
	}

	public boolean isContainingListGetOnly() {
		if (isRoot()) {
			return factory.getRootListData().isGetOnly();
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
		result = prime * result
				+ ((containingListFieldIfNotRoot == null) ? 0 : containingListFieldIfNotRoot.hashCode());
		result = prime * result + containingListSize;
		result = prime * result + ((factory == null) ? 0 : factory.hashCode());
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
		if (containingListFieldIfNotRoot == null) {
			if (other.containingListFieldIfNotRoot != null)
				return false;
		} else if (!containingListFieldIfNotRoot.equals(other.containingListFieldIfNotRoot))
			return false;
		if (containingListSize != other.containingListSize)
			return false;
		if (factory == null) {
			if (other.factory != null)
				return false;
		} else if (!factory.equals(other.factory))
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
