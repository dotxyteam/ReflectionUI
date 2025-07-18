
package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.MiscUtils;

/**
 * This class is a sub-class of {@link ItemPosition} that uses buffers to
 * optimize the access to the list/tree item. The main buffer is actually a copy
 * of the containing list stored either in the parent
 * {@link BufferedItemPosition} or in the associated factory
 * ({@link AbstractBufferedItemPositionFactory}) (when the current item position
 * is root). Additionally a fake item value that overrides the actual item value
 * can be set.
 * 
 * Note that there cannot be 2 similar instances (created from the same factory,
 * at the same depth and the same index).
 * 
 * Note also that calling {@link #updateContainingList(Object[])} will replace
 * the buffered items by those passed as parameter (they may differ from the
 * actual new items that would be retrieved from the underlying object(s)).
 * 
 * @author olitank
 *
 */
public class BufferedItemPosition extends ItemPosition {

	public static final Object NULL_FAKE_ITEM = new Object() {
		@Override
		public String toString() {
			return "NULL_BUFFERED_ITEM_REMEMBERED";
		}

	};
	protected Object fakeItem;
	protected Object bufferedSubListValue;
	protected Object[] bufferedSubListRawValue;
	protected IFieldInfo bufferedSubListField;
	protected WeakHashMap<BufferedItemPosition, Integer> indexByBufferedSubItemPosition = new WeakHashMap<BufferedItemPosition, Integer>();

	protected BufferedItemPosition() {
		super();
	}

	@Override
	public Object getItem() {
		if (fakeItem != null) {
			if (fakeItem == NULL_FAKE_ITEM) {
				return null;
			} else {
				return fakeItem;
			}
		}
		return super.getItem();
	}

	/**
	 * Sets the fake item value that overrides the actual item value. Note that null
	 * is a valid value, not the absence of value. In order to remove the fake item
	 * value {@link #unsetFakeItem()} must be used.
	 * 
	 * @param item The new fake item value. Can be null.
	 */
	public void setFakeItem(Object item) {
		if (item == null) {
			fakeItem = NULL_FAKE_ITEM;
		} else {
			fakeItem = item;
		}
	}

	/**
	 * Removes the fake item value that overrides the actual item value.
	 */
	public void unsetFakeItem() {
		fakeItem = null;
	}

	/**
	 * Updates the containing list buffer. Note that the containing list (including
	 * the current item) may not contain up-to-date values after this operation if
	 * the parent item itself has an old obsolete buffered value.
	 */
	public void refreshContainingList() {
		if (isRoot()) {
			((AbstractBufferedItemPositionFactory) factory).refresh();
		} else {
			((BufferedItemPosition) parentItemPosition).bufferedSubListValue = null;
			((BufferedItemPosition) parentItemPosition).bufferedSubListRawValue = null;
			((BufferedItemPosition) parentItemPosition).bufferedSubListField = null;
		}
	}

	/**
	 * Updates the ancestor lists buffers recursively (from the root to the current
	 * containing list). It ensures that the next call to {@link #getItem()} or
	 * {@link #retrieveContainingListRawValue()} or
	 * {@link #retrieveContainingListValue()} returns up-to-date values (not old
	 * buffered values).
	 */
	public void refreshContainingListsRecursively() {
		if (!isRoot()) {
			getParentItemPosition().refreshContainingListsRecursively();
		}
		refreshContainingList();
	}

	/**
	 * Updates the descendant lists buffers recursively. All existing descendant
	 * item positions will be up-to-date after this operation.
	 */
	public void refreshDescendants() {
		for (int index : indexByBufferedSubItemPosition.values()) {
			for (BufferedItemPosition bufferedSubItemPosition : MiscUtils
					.getKeysFromValue(indexByBufferedSubItemPosition, index)) {
				if (bufferedSubItemPosition != null) {
					bufferedSubItemPosition.refreshDescendants();
				}
			}
		}
		bufferedSubListValue = null;
		bufferedSubListRawValue = null;
		bufferedSubListField = null;
	}

	@Override
	public AbstractBufferedItemPositionFactory getFactory() {
		return (AbstractBufferedItemPositionFactory) super.getFactory();
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		return (BufferedItemPosition) parentItemPosition;
	}

	@Override
	public BufferedItemPosition getRoot() {
		return (BufferedItemPosition) super.getRoot();
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSibling(index2);
		if (result == null) {
			return null;
		}
		result.fakeItem = null;
		return result;
	}

	@Override
	public List<BufferedItemPosition> getPreviousSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getPreviousSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getFollowingSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getFollowingSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public IFieldInfo getSubListField() {
		if (bufferedSubListField == null) {
			bufferedSubListField = super.getSubListField();
		}
		return bufferedSubListField;
	}

	@Override
	public Object retrieveSubListValue() {
		if (bufferedSubListValue == null) {
			bufferedSubListValue = super.retrieveSubListValue();
		}
		return bufferedSubListValue;
	}

	@Override
	public Object[] retrieveSubListRawValue() {
		if (bufferedSubListRawValue == null) {
			bufferedSubListRawValue = super.retrieveSubListRawValue();
		}
		return bufferedSubListRawValue;
	}

	@Override
	public void updateContainingList(Object[] newContainingListRawValue) {
		super.updateContainingList(newContainingListRawValue);
		refreshContainingList();
		changeContainingListBuffer(retrieveContainingListValue(), retrieveContainingListRawValue());
	}

	/**
	 * Updates the buffer of the containing list (or the factory if the current item
	 * position is root). Note that this method is not recursive.
	 * 
	 * @param newContainingListValue    The object that should replace the buffered
	 *                                  containing list.
	 * @param newContainingListRawValue The array that contains the items that
	 *                                  should replace all the buffered containing
	 *                                  list items.
	 */
	public void changeContainingListBuffer(Object newContainingListValue, Object[] newContainingListRawValue) {
		if (isRoot()) {
			getFactory().bufferedRootListValue = newContainingListValue;
			getFactory().bufferedRootListRawValue = Arrays.copyOf(newContainingListRawValue,
					newContainingListRawValue.length);
		} else {
			getParentItemPosition().bufferedSubListValue = newContainingListValue;
			getParentItemPosition().bufferedSubListRawValue = Arrays.copyOf(newContainingListRawValue,
					newContainingListRawValue.length);
		}
	}

	@Override
	public synchronized BufferedItemPosition getSubItemPosition(int index) {
		if (indexByBufferedSubItemPosition.containsValue(index)) {
			for (BufferedItemPosition itemPosition : MiscUtils.getKeysFromValue(indexByBufferedSubItemPosition,
					index)) {
				if (itemPosition != null) {
					itemPosition.containingListFieldIfNotRoot = getSubListField();
					if (itemPosition.containingListFieldIfNotRoot == null) {
						return null;
					}
					return itemPosition;
				}
			}
		}
		BufferedItemPosition result = (BufferedItemPosition) super.getSubItemPosition(index);
		if (result != null) {
			result.bufferedSubListField = null;
			result.bufferedSubListValue = null;
			result.bufferedSubListRawValue = null;
			result.indexByBufferedSubItemPosition = new WeakHashMap<BufferedItemPosition, Integer>();
		}
		indexByBufferedSubItemPosition.put(result, index);
		return result;
	}

	@Override
	public List<BufferedItemPosition> getSubItemPositions() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getSubItemPositions()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fakeItem == null) ? 0 : fakeItem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BufferedItemPosition other = (BufferedItemPosition) obj;
		if (fakeItem == null) {
			if (other.fakeItem != null)
				return false;
		} else if (!fakeItem.equals(other.fakeItem))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}
