package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.control.IFieldControlData;

public class BufferedItemPositionFactory extends ItemPositionFactory {

	protected Object[] bufferedRootListRawValue;

	public BufferedItemPositionFactory(IFieldControlData containingListDataIfRoot) {
		super(containingListDataIfRoot);
	}

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
