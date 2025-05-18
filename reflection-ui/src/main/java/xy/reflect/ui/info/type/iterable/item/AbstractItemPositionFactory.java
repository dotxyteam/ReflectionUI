
package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

/**
 * Base class of factories that create directly or indirectly
 * {@link ItemPosition} instances. Actually these factories only create root
 * item positions that will generate children or sibling item positions.
 * 
 * @author olitank
 *
 */
public abstract class AbstractItemPositionFactory {

	/**
	 * @return the root list value.
	 */
	public abstract Object getRootListValue();

	/**
	 * Updates the root list value.
	 * 
	 * @param rootListValue The new root list value.
	 */
	public abstract void setRootListValue(Object rootListValue);

	/**
	 * @return the type information of the root list value.
	 */
	public abstract IListTypeInfo getRootListType();

	/**
	 * @return root list value return mode.
	 */
	public abstract ValueReturnMode getRootListValueReturnMode();

	/**
	 * @return false if and only if the root list value can be set. Otherwise
	 *         {@link #setRootListValue(Object)} should not be called.
	 */
	public abstract boolean isRootListGetOnly();

	/**
	 * @return the display name of the root list.
	 */
	public abstract String getRootListTitle();

	/**
	 * @return a job (may be null) that restores the state of the list when its form
	 *         was last refreshed.
	 */
	public abstract Runnable getLastFormRefreshStateRestorationJob();

	/**
	 * @return the object (typically a list control) from which this factory was
	 *         created.
	 */
	public abstract Object getSource();

	/**
	 * @param index
	 * @return the root item position for with given index.
	 */
	public ItemPosition getRootItemPosition(int index) {
		ItemPosition result = createItemPosition();
		result.factory = this;
		result.index = index;
		return result;
	}

	/**
	 * @return all the root item positions.
	 */
	public List<ItemPosition> getRootItemPositions() {
		Object[] rootListRawValue = getRootListRawValue();
		if (rootListRawValue == null) {
			return null;
		}
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int i = 0; i < rootListRawValue.length; i++) {
			result.add(getRootItemPosition(i));
		}
		return result;
	}

	/**
	 * @return an array containing the root list items.
	 */
	public Object[] getRootListRawValue() {
		Object rootListValue = getRootListValue();
		if (rootListValue == null) {
			return new Object[0];
		}
		return getRootListType().toArray(rootListValue);
	}

	protected ItemPosition createItemPosition() {
		return new ItemPosition();
	}

}
