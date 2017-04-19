package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

public class ItemPositionFactory {

	protected IFieldControlData rootListData;

	public ItemPositionFactory(IFieldControlData rootListData) {
		super();
		this.rootListData = rootListData;
	}

	public IFieldControlData getRootListData() {
		return rootListData;
	}

	public ItemPosition getRootItemPosition(int index) {
		ItemPosition result = createItemPosition();
		result.factory = this;
		result.parentItemPosition = null;
		result.containingListFieldIfNotRoot = null;
		result.index = index;
		result.containingListSize = retrieveRootListRawValue().length;
		return result;
	}

	protected ItemPosition createItemPosition() {
		return new ItemPosition();
	}

	public Object retrieveRootListValue() {
		return rootListData.getValue();
	}

	public Object[] retrieveRootListRawValue() {
		Object list = retrieveRootListValue();
		if (list == null) {
			return new Object[0];
		}
		return getRootListType().toArray(list);
	}

	public IListTypeInfo getRootListType() {
		return (IListTypeInfo) rootListData.getType();
	}

}
