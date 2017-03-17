package xy.reflect.ui.info.type.iterable.item;

import xy.reflect.ui.info.ValueReturnMode;

public class BufferedItemPosition extends ItemPosition {

	protected Object bufferedItem;
	protected ItemPosition itemPosition;

	public BufferedItemPosition(ItemPosition itemPosition, Object bufferedItem) {
		super(itemPosition.getParentItemPosition(), itemPosition.getContainingListData(), itemPosition.getIndex());
		this.itemPosition = itemPosition;
		this.bufferedItem = bufferedItem;
	}

	@Override
	public Object getItem() {
		return bufferedItem;
	}

	public void setItem(Object item) {
		this.bufferedItem = item;
	}

	@Override
	public ValueReturnMode getItemReturnMode() {
		return ValueReturnMode.SELF;
	}

	public ItemPosition getStandardItemPosition(){
		return itemPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((bufferedItem == null) ? 0 : bufferedItem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BufferedItemPosition other = (BufferedItemPosition) obj;
		if (bufferedItem == null) {
			if (other.bufferedItem != null)
				return false;
		} else if (!bufferedItem.equals(other.bufferedItem))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}