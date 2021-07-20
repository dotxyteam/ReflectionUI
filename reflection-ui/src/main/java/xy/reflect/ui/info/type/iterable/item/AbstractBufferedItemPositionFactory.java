


package xy.reflect.ui.info.type.iterable.item;

import java.util.HashMap;
import java.util.Map;

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
	protected Map<Integer, BufferedItemPosition> bufferedRootItemPositionByIndex = new HashMap<Integer, BufferedItemPosition>();

	protected abstract Object getNonBufferedRootListValue();

	protected abstract void setNonBufferedRootListValue(Object rootListValue);

	@Override
	public BufferedItemPosition getRootItemPosition(int index) {
		if (bufferedRootItemPositionByIndex.containsKey(index)) {
			return bufferedRootItemPositionByIndex.get(index);
		}
		BufferedItemPosition result = (BufferedItemPosition) super.getRootItemPosition(index);
		bufferedRootItemPositionByIndex.put(index, result);
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
		bufferedRootItemPositionByIndex.clear();
	}

	/**
	 * Updates all the buffers of all item positions created (directly or
	 * indirectly) by this factory so that all items will have up-to-date values.
	 */
	public void refreshAll() {
		for (int index : bufferedRootItemPositionByIndex.keySet()) {
			BufferedItemPosition bufferedRootItemPosition = bufferedRootItemPositionByIndex.get(index);
			if (bufferedRootItemPosition == null) {
				continue;
			}
			bufferedRootItemPosition.refreshBranch();
		}
		refresh();
	}

}
