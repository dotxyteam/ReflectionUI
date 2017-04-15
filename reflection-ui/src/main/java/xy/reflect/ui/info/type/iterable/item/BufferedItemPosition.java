package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.control.input.IFieldControlData;

public class BufferedItemPosition extends ItemPosition {

	protected static final Object NULL_BUFFERED_ITEM_REMEMBERED = new Object() {
		@Override
		public String toString() {
			return "NULL_BUFFERED_ITEM_REMEMBERED";
		}

	};
	protected Object bufferedItem;
	protected Object[] bufferedContainingListRawValue;
	protected BufferedItemPosition bufferedAnySubItemPosition;

	public BufferedItemPosition(IFieldControlData containingListDataIfRoot, int index) {
		super(containingListDataIfRoot, index);
	}

	@Override
	public Object getItem() {
		if (bufferedItem == null) {
			setBufferedItem(super.getItem());
		}
		if (bufferedItem == NULL_BUFFERED_ITEM_REMEMBERED) {
			return null;
		} else {
			return bufferedItem;
		}
	}

	public void setBufferedItem(Object item) {
		if (item == null) {
			bufferedItem = NULL_BUFFERED_ITEM_REMEMBERED;
		}else{
			bufferedItem = item;			
		}
	}

	public void refreshBranch() {
		if (!isRoot()) {
			((BufferedItemPosition) parentItemPosition).refreshBranch();
		}
		Object[] newContainingListRawValue = super.retrieveContainingListRawValue();
		if ((index >= 0) && (index < newContainingListRawValue.length)) {
			setBufferedItem(newContainingListRawValue[index]);
		} else {
			setBufferedItem(null);
		}
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		return (BufferedItemPosition) parentItemPosition;
	}

	@Override
	public BufferedItemPosition getRootListItemPosition() {
		return (BufferedItemPosition) super.getRootListItemPosition();
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSibling(index2);
		result.bufferedItem = null;
		return result;
	}

	@Override
	public List<BufferedItemPosition> getPreviousSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getPreviousSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getFollowingSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getFollowingSiblings()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public BufferedItemPosition getAnySubItemPosition() {
		if (bufferedAnySubItemPosition == null) {
			bufferedAnySubItemPosition = (BufferedItemPosition) super.getAnySubItemPosition();
			if (bufferedAnySubItemPosition != null) {
				bufferedAnySubItemPosition.bufferedContainingListRawValue = null;
				bufferedAnySubItemPosition.retrieveContainingListRawValue();
				bufferedAnySubItemPosition.containingListSize = bufferedAnySubItemPosition.bufferedContainingListRawValue.length;
			}
		}
		return bufferedAnySubItemPosition;

	}

	@Override
	public Object[] retrieveContainingListRawValue() {
		if (bufferedContainingListRawValue == null) {
			bufferedContainingListRawValue = super.retrieveContainingListRawValue();
		}
		return bufferedContainingListRawValue;
	}

	@Override
	public List<BufferedItemPosition> getSubItemPositions() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition i : super.getSubItemPositions()) {
			result.add((BufferedItemPosition) i);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getItem() == null) ? 0 : getItem().hashCode());
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
		if (getItem() == null) {
			if (other.getItem() != null)
				return false;
		} else if (!getItem().equals(other.getItem()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}