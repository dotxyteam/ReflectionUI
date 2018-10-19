package xy.reflect.ui.info.type.iterable.item;

public abstract class AbstractBufferedItemPositionFactory extends AbstractItemPositionFactory {

	protected Object[] bufferedRootListRawValue;

	@Override
	public BufferedItemPosition getRootItemPosition(int index) {
		return (BufferedItemPosition) super.getRootItemPosition(index);
	}

	@Override
	protected BufferedItemPosition createItemPosition() {
		return new BufferedItemPosition();
	}

	@Override
	public Object[] retrieveRootListRawValue() {
		if (bufferedRootListRawValue == null) {
			bufferedRootListRawValue = super.retrieveRootListRawValue();
		}
		return bufferedRootListRawValue;
	}

	public void refresh() {
		bufferedRootListRawValue = null;
	}

}
