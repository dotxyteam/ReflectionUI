package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.input.DefaultFieldControlData;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class ItemPosition implements Cloneable {

	protected ItemPosition parentItemPosition;
	protected IFieldControlData containingListData;
	protected int index;

	protected ItemPosition(ItemPosition parentItemPosition, IFieldControlData containingListData, int index) {
		super();
		this.parentItemPosition = parentItemPosition;
		this.containingListData = containingListData;
		this.index = index;
	}

	public ItemPosition(ItemPosition parentItemPosition, int index) {
		this.parentItemPosition = parentItemPosition;
		this.index = index;
	}

	public ItemPosition(IFieldControlData containingListData, int index) {
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
		Object[] containingListRawValue = getContainingListRawValue();
		if ((index >= 0) && (index < containingListRawValue.length)) {
			return containingListRawValue[index];
		} else {
			return null;
		}
	}

	public String getContainingListTitle() {
		return getContainingListData().getCaption();
	}

	public IFieldControlData getContainingListData() {
		if (parentItemPosition == null) {
			return getRootListData();
		}
		return parentItemPosition.getSubListData();
	}

	public Object[] getContainingListRawValue() {
		Object list = getContainingListData().getValue();
		if (list == null) {
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
		return result;
	}

	public IFieldControlData getSubListData() {
		IListStructuralInfo treeInfo = getRootListItemPosition().getContainingListType().getStructuralInfo();
		if (treeInfo == null) {
			return null;
		}
		BufferedItemPosition ghostItemPosition = new BufferedItemPosition(this, getItem());
		final IFieldInfo subListField = treeInfo.getItemSubListField(ghostItemPosition);
		if (subListField == null) {
			return null;
		}
		return new DefaultFieldControlData(ghostItemPosition.getItem(), subListField);
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		IFieldControlData subListData = getSubListData();
		if (subListData == null) {
			return Collections.emptyList();
		}
		ItemPosition anySubItemPosition = clone();
		anySubItemPosition.parentItemPosition = this;
		anySubItemPosition.containingListData = subListData;
		anySubItemPosition.index = -1;
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < anySubItemPosition.getContainingListRawValue().length; i++) {
			result.add(anySubItemPosition.getSibling(i));
		}
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

	public IFieldControlData getRootListData() {
		return getRootListItemPosition().containingListData;
	}

	public ValueReturnMode getItemReturnMode() {
		ValueReturnMode result = ValueReturnMode.combine(getContainingListData().getValueReturnMode(),
				getContainingListType().getItemReturnMode());
		if (parentItemPosition != null) {
			result = ValueReturnMode.combine(parentItemPosition.getItemReturnMode(), result);
		}
		return result;
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
