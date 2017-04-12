package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;

public class BufferedItemPosition extends ItemPosition {

	protected ItemPosition standardItemPosition;
	protected Object bufferedItem;

	public BufferedItemPosition(ItemPosition standardItemPosition) {
		super(null, standardItemPosition.containingListDataIfRoot, standardItemPosition.containingListFieldIfNotRoot,
				standardItemPosition.containingListSize, standardItemPosition.index);
		if (standardItemPosition instanceof BufferedItemPosition) {
			throw new ReflectionUIError();
		}
		this.standardItemPosition = standardItemPosition;
		this.parentItemPosition = createBufferedParentItemPosition();
	}

	protected BufferedItemPosition createBufferedParentItemPosition() {
		ItemPosition standardParentItemPosition = standardItemPosition.parentItemPosition;
		if (standardParentItemPosition != null) {
			parentItemPosition = new BufferedItemPosition(standardParentItemPosition);
		}
		return null;
	}

	@Override
	public Object getItem() {
		if (bufferedItem == null) {
			if ((index >= 0) && (index < containingListSize)) {
				refreshBufferedItemFromParent();
			}
		}
		return bufferedItem;
	}

	public void setBufferedItem(Object item) {
		this.bufferedItem = item;
	}

	public void refreshBufferedItemFromRoot() {
		setBufferedItem(standardItemPosition.getItem());
	}

	public void refreshBufferedItemFromParent() {
		setBufferedItem(super.getItem());
	}

	public ItemPosition getStandardItemPosition() {
		return standardItemPosition;
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
		BufferedItemPosition result = (BufferedItemPosition) super.getAnySubItemPosition();
		if (result != null) {
			result.standardItemPosition = new ItemPosition(standardItemPosition, null,
					result.containingListFieldIfNotRoot, result.containingListSize, result.index);
			result.parentItemPosition = this;
			result.bufferedItem = null;
		}
		return result;
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
		result = prime * result + ((bufferedItem == null) ? 0 : bufferedItem.hashCode());
		result = prime * result + ((standardItemPosition == null) ? 0 : standardItemPosition.hashCode());
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
		if (standardItemPosition == null) {
			if (other.standardItemPosition != null)
				return false;
		} else if (!standardItemPosition.equals(other.standardItemPosition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}