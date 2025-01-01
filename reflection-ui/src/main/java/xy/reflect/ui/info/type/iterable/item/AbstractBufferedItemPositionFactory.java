
package xy.reflect.ui.info.type.iterable.item;

import java.util.WeakHashMap;

import xy.reflect.ui.util.MiscUtils;

/**
 * This class is a sub-class of {@link AbstractItemPositionFactory} that only
 * creates {@link BufferedItemPosition} instances.
 * 
 * @author olitank
 *
 */
public abstract class AbstractBufferedItemPositionFactory extends AbstractItemPositionFactory {

	protected Object[] bufferedRootListRawValue;
	protected Object bufferedRootListValue;
	protected WeakHashMap<BufferedItemPosition, Integer> indexByBufferedRootItemPosition = new WeakHashMap<BufferedItemPosition, Integer>();

	protected abstract Object getNonBufferedRootListValue();

	protected abstract void setNonBufferedRootListValue(Object rootListValue);

	@Override
	public synchronized BufferedItemPosition getRootItemPosition(int index) {
		if (indexByBufferedRootItemPosition.containsValue(index)) {
			for (BufferedItemPosition itemPosition : MiscUtils.getKeysFromValue(indexByBufferedRootItemPosition,
					index)) {
				if (itemPosition != null) {
					return itemPosition;
				}
			}
		}
		BufferedItemPosition result = (BufferedItemPosition) super.getRootItemPosition(index);
		indexByBufferedRootItemPosition.put(result, index);
		return result;
	}

	@Override
	protected BufferedItemPosition createItemPosition() {
		return new BufferedItemPosition();
	}

	@Override
	public Object getRootListValue() {
		if (bufferedRootListValue == null) {
			bufferedRootListValue = getNonBufferedRootListValue();
		}
		return bufferedRootListValue;
	}

	@Override
	public Object[] getRootListRawValue() {
		if (bufferedRootListRawValue == null) {
			bufferedRootListRawValue = super.getRootListRawValue();
		}
		return bufferedRootListRawValue;
	}

	@Override
	public void setRootListValue(Object rootListValue) {
		setNonBufferedRootListValue(rootListValue);
		refresh();
	}

	/**
	 * Updates the root list buffer so that root list items will have up-to-date
	 * values.
	 */
	public void refresh() {
		bufferedRootListValue = null;
		bufferedRootListRawValue = null;
	}

	/**
	 * Updates all the buffers of all item positions created (directly or
	 * indirectly) by this factory so that all items will have up-to-date values.
	 */
	public void refreshAll() {
		for (int index : indexByBufferedRootItemPosition.values()) {
			for (BufferedItemPosition bufferedRootItemPosition : MiscUtils
					.getKeysFromValue(indexByBufferedRootItemPosition, index)) {
				if (bufferedRootItemPosition != null) {
					bufferedRootItemPosition.refreshBranch();
				}
			}
		}
		refresh();
	}

}
