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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootListData == null) ? 0 : rootListData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemPositionFactory other = (ItemPositionFactory) obj;
		if (rootListData == null) {
			if (other.rootListData != null)
				return false;
		} else if (!rootListData.equals(other.rootListData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ItemPositionFactory [rootListData=" + rootListData + "]";
	}
	
	

}
