package xy.reflect.ui.info.type.iterable.item;

public abstract class BufferedItemPositionFactory extends ItemPositionFactory {

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
	public Object[] retrieveRootListRawValue(Object rootListValue) {
		if (bufferedRootListRawValue == null) {
			bufferedRootListRawValue = super.retrieveRootListRawValue(rootListValue);
		}
		return bufferedRootListRawValue;
	}

	public void refresh() {
		bufferedRootListRawValue = null;
	}

}
