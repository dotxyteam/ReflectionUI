package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This the class of factories that create directly or indirectly
 * {@link ItemPosition} instances. Actually these factories only create root
 * item positions that will generate children or sibling item positions.
 * 
 * @author olitank
 *
 */
public class ItemPositionFactory extends AbstractItemPositionFactory {

	protected IFieldControlData listData;
	protected Object source;

	/**
	 * Instantiates this class.
	 * 
	 * @param listData The field control data that will be used to obtain the list
	 *                 value or update it.
	 */
	public ItemPositionFactory(IFieldControlData listData, Object source) {
		if (!(listData.getType() instanceof IListTypeInfo)) {
			throw new ReflectionUIError();
		}
		this.listData = listData;
		this.source = source;
	}

	@Override
	public Object getSource() {
		return source;
	}

	@Override
	public Object getRootListValue() {
		return listData.getValue();
	}

	@Override
	public void setRootListValue(Object rootListValue) {
		listData.setValue(rootListValue);
	}

	@Override
	public IListTypeInfo getRootListType() {
		return (IListTypeInfo) listData.getType();
	}

	@Override
	public ValueReturnMode getRootListValueReturnMode() {
		return listData.getValueReturnMode();
	}

	@Override
	public boolean isRootListGetOnly() {
		return listData.isGetOnly();
	}

	@Override
	public String getRootListTitle() {
		return listData.getCaption();
	}

	@Override
	public Runnable getLastFormRefreshStateRestorationJob() {
		return listData.getLastFormRefreshStateRestorationJob();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listData == null) ? 0 : listData.hashCode());
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
		if (listData == null) {
			if (other.listData != null)
				return false;
		} else if (!listData.equals(other.listData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ItemPositionfactory [listData=" + listData + "]";
	}

}