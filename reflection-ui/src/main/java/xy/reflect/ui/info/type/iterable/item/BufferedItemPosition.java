package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.util.ReflectionUIError;

public class BufferedItemPosition extends ItemPosition {

	protected Object bufferedItem;
	protected ItemPosition standardItemPosition;

	public BufferedItemPosition(ItemPosition standardItemPosition, Object bufferedItem) {
		super(standardItemPosition.getParentItemPosition(), standardItemPosition.getContainingListData(),
				standardItemPosition.getIndex());
		if (standardItemPosition instanceof BufferedItemPosition) {
			throw new ReflectionUIError();
		}
		this.standardItemPosition = standardItemPosition;
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
		return ValueReturnMode.SELF_OR_PROXY;
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		ItemPosition standardParentItemPosition = getStandardItemPosition().parentItemPosition;
		if (standardParentItemPosition == null) {
			return null;
		}
		return new BufferedItemPosition(standardParentItemPosition, standardParentItemPosition.getItem());
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		ItemPosition standardSibling = getStandardItemPosition().getSibling(index2);
		return new BufferedItemPosition(standardSibling, standardSibling.getItem());
	}

	@Override
	public List<BufferedItemPosition> getPreviousSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardPreviousPosition : getStandardItemPosition().getPreviousSiblings()) {
			result.add(new BufferedItemPosition(standardPreviousPosition, standardPreviousPosition.getItem()));
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getFollowingSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardFollowingPosition : getStandardItemPosition().getFollowingSiblings()) {
			result.add(new BufferedItemPosition(standardFollowingPosition, standardFollowingPosition.getItem()));
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getSubItemPositions() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardSubItemPosition : getStandardItemPosition().getSubItemPositions()) {
			result.add(new BufferedItemPosition(standardSubItemPosition, standardSubItemPosition.getItem()));
		}
		return result;
	}

	@Override
	public BufferedItemPosition getRootListItemPosition() {
		return (BufferedItemPosition) super.getRootListItemPosition();
	}

	public ItemPosition getStandardItemPosition() {
		return standardItemPosition;
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