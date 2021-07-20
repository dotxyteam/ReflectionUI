


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
 * This class describes the spatial position of an item in a list/tree.
 * 
 * It actually gives access to the successive objects and fields that were used
 * in order to get a specific item value. Calling {@link #getItem()} will then
 * re-access these objects and fields and return an up-to-date item value.
 * 
 * Every item position is then bound to a containing list that would hold its
 * item. But it may not reference an actual item if its index (specifying its
 * location in the containing list) is out of bounds.
 * 
 * @author olitank
 *
 */
public class ItemPosition implements Cloneable {

	protected AbstractItemPositionFactory factory;
	protected ItemPosition parentItemPosition;
	protected int index;
	protected IFieldInfo containingListFieldIfNotRoot;

	/**
	 * Default constructor. It does not properly initialize the fields. Should be
	 * called only by this class, subclasses and factories.
	 */
	protected ItemPosition() {
	}

	/**
	 * @return the factory that created directly or indirectly this object. The
	 *         factory typically creates root item positions that may create sibling
	 *         or children item positions.
	 */
	public AbstractItemPositionFactory getFactory() {
		return factory;
	}

	/**
	 * @return the field that is used to get the containing list from the parent
	 *         item or null if the item position is at the root.
	 */
	public IFieldInfo getContainingListFieldIfNotRoot() {
		return containingListFieldIfNotRoot;
	}

	/**
	 * @return the size of the containing list.
	 */
	public int getContainingListSize() {
		return retrieveContainingListRawValue().length;
	}

	/**
	 * @return the parent of this item position or null (for a root item position).
	 */
	public ItemPosition getParentItemPosition() {
		return parentItemPosition;
	}

	/**
	 * @return the current index in the containing list.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param object The object that must be checked.
	 * @return whether this given object could be an item at this position or not.
	 */
	public boolean supportsItem(Object object) {
		IListTypeInfo listType = getContainingListType();
		ITypeInfo itemType = listType.getItemType();
		if (itemType != null) {
			if (!itemType.supports(object)) {
				return false;
			}
		}
		if (object == null) {
			if (!listType.isItemNullValueSupported()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the type information of the containing list.
	 */
	public IListTypeInfo getContainingListType() {
		if (isRoot()) {
			return factory.getRootListType();
		} else {
			return (IListTypeInfo) containingListFieldIfNotRoot.getType();
		}
	}

	/**
	 * @return the title to be given to the containing list.
	 */
	public String getContainingListTitle() {
		if (isRoot()) {
			return factory.getRootListTitle();
		} else {
			return containingListFieldIfNotRoot.getCaption();
		}
	}

	/**
	 * @return the item at this position.
	 */
	public Object getItem() {
		Object[] containingListRawValue;
		if (isRoot()) {
			containingListRawValue = factory.getRootListRawValue();
		} else {
			containingListRawValue = parentItemPosition.retrieveSubListRawValue();
		}
		if ((index >= 0) && (index < containingListRawValue.length)) {
			return containingListRawValue[index];
		} else {
			return null;
		}
	}

	/**
	 * @return the number of ancestors of this item position (0 for a root item
	 *         position).
	 */
	public int getDepth() {
		int result = 0;
		ItemPosition current = this;
		while (current.getParentItemPosition() != null) {
			current = current.getParentItemPosition();
			result++;
		}
		return result;
	}

	/**
	 * @return all the previous item positions (their {@link #getIndex()} would
	 *         return a value from 0 to the current {@link #getIndex()} - 1.
	 */
	public List<? extends ItemPosition> getPreviousSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < getIndex(); i++) {
			result.add(getSibling(i));
		}
		Collections.reverse(result);
		return result;
	}

	/**
	 * @return all the following item positions (their {@link #getIndex()} would
	 *         return a value from {@link #getIndex()} + 1 to
	 *         {@link #getContainingListSize()} - 1.
	 */
	public List<? extends ItemPosition> getFollowingSiblings() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		int containingListSize = getContainingListSize();
		for (int i = getIndex() + 1; i < containingListSize; i++) {
			result.add(getSibling(i));
		}
		return result;
	}

	/**
	 * @return the list of ancestors of this item position (empty list for a root
	 *         item position).
	 */
	public List<ItemPosition> getAncestors() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		ItemPosition ancestor = getParentItemPosition();
		while (ancestor != null) {
			result.add(ancestor);
			ancestor = ancestor.getParentItemPosition();
		}
		return result;
	}

	/**
	 * @return a clone of this item positions with {@link #getIndex()} returning the
	 *         given index.
	 */
	public ItemPosition getSibling(int index) {
		if(isRoot()) {
			return factory.getRootItemPosition(index);
		}else {
			return parentItemPosition.getSubItemPosition(index);
		}
	}

	/**
	 * @return the containing list.
	 */
	public Object retrieveContainingListValue() {
		if (isRoot()) {
			return factory.getRootListValue();
		} else {
			return parentItemPosition.retrieveSubListValue();
		}
	}

	/**
	 * @return all the containing list items packed in an array.
	 */
	public Object[] retrieveContainingListRawValue() {
		if (isRoot()) {
			return factory.getRootListRawValue();
		} else {
			return parentItemPosition.retrieveSubListRawValue();
		}
	}

	/**
	 * @return the sub-list items packed in an array (may be null if there is no
	 *         sub-list or if the sub-list value is null).
	 */
	public Object[] retrieveSubListRawValue() {
		Object subListValue = retrieveSubListValue();
		if (subListValue == null) {
			return null;
		} else {
			IListTypeInfo subListType = (IListTypeInfo) getSubListField().getType();
			return subListType.toArray(subListValue);
		}
	}

	/**
	 * @return the sub-list (may be null if there is no sub-list or if the sub-list
	 *         value is null).
	 */
	public Object retrieveSubListValue() {
		IFieldInfo subListField = getSubListField();
		if (subListField == null) {
			return null;
		}
		final Object item = getItem();
		return subListField.getValue(item);
	}

	/**
	 * @return the field that gets the sub-list from the current item or null if
	 *         there is no sub-list.
	 */
	public IFieldInfo getSubListField() {
		IListStructuralInfo treeInfo = getContainingListType().getStructuralInfo();
		if (treeInfo == null) {
			return null;
		}
		return treeInfo.getItemSubListField(this);
	}

	/**
	 * @param index The value that the resulting sub-item position
	 *              {@link #getIndex()} method would return.
	 * @return a position referencing an item in the sub-list (may be null if there
	 *         is no sub-list or if the sub-list value is null).
	 */
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

	/**
	 * @return positions of items in the sub-list. An empty list is returned if
	 *         there is no sub-list.
	 */
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

	/**
	 * @return whether the current item position is located at the root or not.
	 */
	public boolean isRoot() {
		return parentItemPosition == null;
	}

	/**
	 * @return the root item position that is an ancestor of this item position.
	 */
	public ItemPosition getRoot() {
		ItemPosition current = this;
		while (!current.isRoot()) {
			current = current.getParentItemPosition();
		}
		return current;
	}

	/**
	 * @return return the value return mode of the item at this position.
	 */
	public ValueReturnMode getItemReturnMode() {
		ValueReturnMode result = ValueReturnMode.combine(geContainingListReturnMode(),
				getContainingListType().getItemReturnMode());
		if (parentItemPosition != null) {
			result = ValueReturnMode.combine(parentItemPosition.getItemReturnMode(), result);
		}
		return result;
	}

	/**
	 * @return return the value return mode of the containing list.
	 */
	public ValueReturnMode geContainingListReturnMode() {
		if (isRoot()) {
			return factory.getRootListValueReturnMode();
		} else {
			return containingListFieldIfNotRoot.getValueReturnMode();
		}
	}

	/**
	 * @return false if and only if the containing list value can be set.
	 */
	public boolean isContainingListGetOnly() {
		if (isRoot()) {
			return factory.isRootListGetOnly();
		} else {
			return containingListFieldIfNotRoot.isGetOnly();
		}
	}

	/**
	 * @return whether the containing list content can be durably changed or not
	 *         (would imply that eventual changes made to the item at this position
	 *         would be lost).
	 */
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

	/**
	 * Updates the containing list so that it will only contain the given items. If
	 * the containing list has a parent item then the parent item field that hosts
	 * the containing list value will be updated and the current method will be
	 * called recursively on the parent item position with its new containing list.
	 * Otherwise only the factory root list value is updated.
	 * 
	 * Note that this method must not be called if
	 * {@link #isContainingListEditable()} returns false. Note also that the
	 * containing list reference may be altered by this operation.
	 * 
	 * @param newContainingListRawValue The array that contains the items that
	 *                                  should replace all the containing list
	 *                                  items.
	 */
	public void updateContainingList(Object[] newContainingListRawValue) {
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
						if (isRoot()) {
							getFactory().setRootListValue(containingListValue);
						} else {
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
						getFactory().setRootListValue(containingListValue);
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
			parentItemPosition.updateContainingList(parentItemContainingListRawValue);
		}
	}

	/**
	 * Validates that all the given items are supported by the containing list.
	 * 
	 * @param listRawValue An array containing the items to be checked.
	 * @throws ReflectionUIError if at least one of the given items is not supported
	 *                           by the containing list.
	 */
	public void checkContainingListRawValue(Object[] listRawValue) {
		IListTypeInfo listType = getContainingListType();
		ITypeInfo itemType = listType.getItemType();
		if (itemType != null) {
			for (Object item : listRawValue) {
				if (item != null) {
					if (!supportsItem(item)) {
						throw new ReflectionUIError("Item not supported: '" + item + "'. Was expecting instance of '"
								+ itemType.getName() + "'");

					}
				}
			}
		}
	}

	/**
	 * @return a string describing the hierarchical location of the current item
	 *         position.
	 */
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
