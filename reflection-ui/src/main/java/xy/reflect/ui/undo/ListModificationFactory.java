


package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * Factory that creates {@link IModification} instances for lists. An
 * {@link ItemPosition} is used to find the containing list that will be updated
 * by the created modifications.
 * 
 * @author olitank
 *
 */
public class ListModificationFactory {

	protected ItemPosition anyItemPosition;

	/**
	 * Builds instances that can create modifications for the list containing the
	 * item referenced by the given {@link ItemPosition}.
	 * 
	 * @param anyListItemPosition An item position in the targeted containing list.
	 *                            Note that the index of the item position may not
	 *                            be valid.
	 */
	public ListModificationFactory(ItemPosition anyListItemPosition) {
		this.anyItemPosition = anyListItemPosition;
	}

	/**
	 * @param newListRawValue
	 * @return a modification that replaces the content of the containing list with
	 *         the specified array items
	 */
	public IModification createListModification(Object[] newListRawValue) {
		return new ListModification(anyItemPosition, newListRawValue, anyItemPosition.retrieveContainingListRawValue());
	}

	/**
	 * @param index The future zero-based position of the new item in the containing
	 *              list.
	 * @return whether an item can be inserted in the containing list at the
	 *         specified zero-based position or not.
	 */
	public boolean canAdd(int index) {
		if ((index < 0) || (index > anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	/**
	 * @param index The future zero-based position of the first item to be inserted
	 *              in the containing list.
	 * @param items The items that need to be inserted.
	 * @return whether the given items can be inserted in the containing list at the
	 *         specified zero-based position or not.
	 */
	public boolean canAddAll(int index, List<Object> items) {
		if (!canAdd(index)) {
			return false;
		}
		for (Object item : items) {
			if (!anyItemPosition.supportsItem(item)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param index   The future zero-based position of the new item in the
	 *                containing list.
	 * @param newItem The item that needs to be inserted.
	 * @return a modification that updates the content of the containing list by
	 *         inserting the specified item at the specified zero-based position.
	 */
	public IModification add(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(newListRawValue);
	}

	/**
	 * @param index The zero-based position of the item that needs to be removed
	 *              from the containing list.
	 * @return whether an item can be removed from the containing list at the
	 *         specified zero-based position or not.
	 */
	public boolean canRemove(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	/**
	 * @param index The zero-based position of the item that needs to be removed
	 *              from the containing list.
	 * @return a modification that updates the content of the containing list by
	 *         removing the item at the specified zero-based position.
	 */
	public IModification remove(int index) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.remove(index);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(newListRawValue);
	}

	/**
	 * @param index The zero-based position in containing list of the item that
	 *              needs to be replaced.
	 * @return whether the item at the specified zero-based position in the
	 *         containing list can be replaced or not.
	 */
	public boolean canSet(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return anyItemPosition.isContainingListEditable();
	}

	/**
	 * @param index The zero-based position in containing list of the item that
	 *              needs to be replaced.
	 * @param item  The new item that must be set.
	 * @return whether the item at the specified zero-based position in the
	 *         containing list can be replaced with the specified item (its type is
	 *         checked) or not.
	 */
	public boolean canSet(int index, Object item) {
		if (!canSet(index)) {
			return false;
		}
		if (!anyItemPosition.supportsItem(item)) {
			return false;
		}
		return true;
	}

	/**
	 * @param index   The zero-based position in containing list of the item that
	 *                needs to be replaced.
	 * @param newItem The new item that must be set.
	 * @return a modification that updates the content of the containing list by
	 *         replacing the item at the specified zero-based position with the
	 *         given item.
	 */
	public IModification set(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.set(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(newListRawValue);
	}

	/**
	 * @param index  The zero-based position in containing list of the item that
	 *               needs to be moved.
	 * @param offset The number of positions of the offset (may be negative).
	 * @return whether the item at the specified zero-based position in the
	 *         containing list can be shifted by the specified number of positions
	 *         or not.
	 */
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

	/**
	 * @param index  The zero-based position in containing list of the item that
	 *               needs to be moved.
	 * @param offset The number of positions of the offset (may be negative).
	 * @return a modification that updates the content of the containing list by
	 *         shifting the item at the specified zero-based position by the
	 *         specified number of positions.
	 */
	public IModification move(int index, int offset) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index + offset, tmpList.remove(index));
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(newListRawValue);
	}

	/**
	 * @return whether all the containing list items can be removed or not.
	 */
	public boolean canClear() {
		return anyItemPosition.isContainingListEditable();
	}

	/**
	 * @return a modification that updates the content of the containing list by
	 *         removing all the items.
	 */
	public IModification clear() {
		return createListModification(new Object[0]);
	}

	/**
	 * Class of modifications that update lists.
	 * 
	 * @author olitank
	 *
	 */
	protected static class ListModification implements IModification {

		protected ItemPosition itemPosition;
		protected Object[] newListRawValue;
		protected Object[] oldListRawValue;

		public ListModification(ItemPosition itemPosition, Object[] newListRawValue, Object[] oldListRawValue) {
			this.itemPosition = itemPosition;
			this.newListRawValue = newListRawValue;
			this.oldListRawValue = oldListRawValue;
		}

		@Override
		public String getTitle() {
			return FieldControlDataModification.getTitle(itemPosition.getContainingListTitle());
		}

		@Override
		public IModification applyAndGetOpposite() {
			itemPosition.updateContainingList(newListRawValue);
			return new ListModification(itemPosition, oldListRawValue, newListRawValue);
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isFake() {
			return false;
		}

	}

}
