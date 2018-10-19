package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

public abstract class AbstractItemPositionFactory {

	public abstract Object getRootListValue();

	public abstract IListTypeInfo getRootListType();

	public abstract ValueReturnMode getRootListValueReturnMode();

	public abstract boolean isRootListGetOnly();

	public abstract String getRootListTitle();

	public ItemPosition getRootItemPosition(int index) {
		ItemPosition result = createItemPosition();
		result.factory = this;
		result.parentItemPosition = null;
		result.containingListFieldIfNotRoot = null;
		result.index = index;
		return result;
	}

	protected ItemPosition createItemPosition() {
		return new ItemPosition();
	}

	public Object[] retrieveRootListRawValue() {
		Object rootListValue = getRootListValue();
		if (rootListValue == null) {
			return new Object[0];
		}
		return getRootListType().toArray(rootListValue);
	}

}
