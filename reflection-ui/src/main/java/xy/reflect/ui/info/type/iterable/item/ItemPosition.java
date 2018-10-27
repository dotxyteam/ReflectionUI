package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This class describe the spatial position of an item in a list/tree.
 * 
 * It actually gives access to the successive objects and fields that were used
 * in order to get a specific item value. Calling {@link #getItem()} will then
 * re-access these objects and fields and return an up-to-date item value.
 * 
 * @author olitank
 *
 */
public class ItemPosition implements Cloneable {

	protected AbstractItemPositionFactory factory;
	protected ItemPosition parentItemPosition;
	protected int index;
	protected IFieldInfo containingListFieldIfNotRoot;

	protected ItemPosition() {
		super();
	}

	public AbstractItemPositionFactory getFactory() {
		return factory;
	}

	public IFieldInfo getContainingListFieldIfNotRoot() {
		return containingListFieldIfNotRoot;
	}

	public int getContainingListSize() {
		return retrieveContainingListRawValue().length;
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
			return factory.getRootListTitle();
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
		int containingListSize = getContainingListSize();
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
			return factory.getRootListValue();
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
			return factory.getRootListValueReturnMode();
		} else {
			return containingListFieldIfNotRoot.getValueReturnMode();
		}
	}

	public boolean isContainingListGetOnly() {
		if (isRoot()) {
			return factory.isRootListGetOnly();
		} else {
			return containingListFieldIfNotRoot.isGetOnly();
		}
	}

	public boolean isContainingListEditable() {
		if (!isRoot()) {
			ItemPosition parentItemPosition = getParentItemPosition();
			if (!parentItemPosition.isContainingListEditable()) {
				return false;
			}
		}
		IListTypeInfo listType = getContainingListType();
		if (listType.canReplaceContent()) {
			Object containingListValue = retrieveContainingListValue();
			if (containingListValue != null) {
				if (geContainingListReturnMode() == ValueReturnMode.DIRECT_OR_PROXY) {
					return true;
				}
				if (!isContainingListGetOnly()) {
					return true;
				}
			}
		}
		if (listType.canInstanciateFromArray()) {
			if (!isContainingListGetOnly()) {
				return true;
			}
		}
		return false;
	}

	public Object updateContainingList(Object[] newContainingListRawValue) {
		Object newRootListValue = null;
		boolean done = false;
		ItemPosition parentItemPosition = getParentItemPosition();
		Object parentItem = isRoot() ? null : parentItemPosition.getItem();

		checkContainingListRawValue(newContainingListRawValue);
		IListTypeInfo listType = getContainingListType();
		if (listType.canReplaceContent()) {
			Object containingListValue = retrieveContainingListValue();
			if (containingListValue != null) {
				if (!done) {
					if (geContainingListReturnMode() == ValueReturnMode.DIRECT_OR_PROXY) {
						listType.replaceContent(containingListValue, newContainingListRawValue);
						done = true;
					}
				}
				if (!done) {
					if (!isContainingListGetOnly()) {
						listType.replaceContent(containingListValue, newContainingListRawValue);
						if (!isRoot()) {
							getContainingListFieldIfNotRoot().setValue(parentItem, containingListValue);
						}
						done = true;
					}
				}
			}
		}
		if (!done) {
			if (listType.canInstanciateFromArray()) {
				if (!isContainingListGetOnly()) {
					Object containingListValue = listType.fromArray(newContainingListRawValue);
					if (isRoot()) {
						newRootListValue = containingListValue;
					} else {
						getContainingListFieldIfNotRoot().setValue(parentItem, containingListValue);
					}
					done = true;
				}
			}
		}

		if (!done) {
			throw new ReflectionUIError();
		}

		if (!isRoot()) {
			Object[] parentItemContainingListRawValue = getParentItemPosition().retrieveContainingListRawValue();
			if (parentItem != parentItemContainingListRawValue[parentItemPosition.getIndex()]) {
				parentItemContainingListRawValue[parentItemPosition.getIndex()] = parentItem;
			}
			newRootListValue = parentItemPosition.updateContainingList(parentItemContainingListRawValue);
		}

		return newRootListValue;
	}

	public void checkContainingListRawValue(Object[] listRawValue) {
		IListTypeInfo listType = getContainingListType();
		ITypeInfo itemType = listType.getItemType();
		if (itemType != null) {
			for (Object item : listRawValue) {
				if (item != null) {
					if (!itemType.supportsInstance(item)) {
						throw new ReflectionUIError("Item not supported: '" + item + "'. Was expecting instance of '"
								+ itemType.getName() + "'");

					}
				}
			}
		}
	}

	public String getPath() {
		StringBuilder result = new StringBuilder();
		ItemPosition current = this;
		while (current != null) {
			String indexString;
			if (current.index == -1) {
				indexString = "";
			} else {
				indexString = Integer.toString(current.index + 1);
			}
			if (current == this) {
				result.insert(0, "Item" + indexString);
			} else {
				result.insert(0, "Item" + indexString + "->Sub");
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
		return "ItemPosition [path=" + getPath() + "]";
	}

}
