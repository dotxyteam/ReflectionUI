package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;

public class BufferedItemPosition extends ItemPosition {

	protected ItemPosition standardItemPosition;
	protected Object bufferedItem;
	protected BufferedItemPosition bufferedParentItemPosition;

	public BufferedItemPosition(ItemPosition standardItemPosition) {
		super(standardItemPosition.parentItemPosition, standardItemPosition.containingListDataIfRoot,
				standardItemPosition.containingListFieldIfNotRoot, standardItemPosition.containingListSize,
				standardItemPosition.index);
		if (standardItemPosition instanceof BufferedItemPosition) {
			throw new ReflectionUIError();
		}
		this.standardItemPosition = standardItemPosition;
	}

	@Override
	public Object getItem() {
		if (bufferedItem == null) {
			if ((index >= 0) && (index < containingListSize)) {
				refreshBufferedItem();
			}
		}
		return bufferedItem;
	}

	public void setBufferedItem(Object item) {
		this.bufferedItem = item;
	}

	public void refreshBufferedItem() {
		setBufferedItem(standardItemPosition.getItem());
	}

	public ItemPosition getStandardItemPosition() {
		return standardItemPosition;
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		if (bufferedParentItemPosition == null) {
			ItemPosition standardParentItemPosition = standardItemPosition.parentItemPosition;
			if (standardParentItemPosition == null) {
				return null;
			}
			bufferedParentItemPosition = new BufferedItemPosition(standardParentItemPosition);
		}
		return bufferedParentItemPosition;
	}

	@Override
	public BufferedItemPosition getRootListItemPosition() {
		return (BufferedItemPosition) super.getRootListItemPosition();
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		ItemPosition standardSibling = standardItemPosition.getSibling(index2);
		return new BufferedItemPosition(standardSibling);
	}

	@Override
	public List<BufferedItemPosition> getPreviousSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardPreviousPosition : standardItemPosition.getPreviousSiblings()) {
			result.add(new BufferedItemPosition(standardPreviousPosition));
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getFollowingSiblings() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardFollowingPosition : standardItemPosition.getFollowingSiblings()) {
			result.add(new BufferedItemPosition(standardFollowingPosition));
		}
		return result;
	}

	@Override
	public BufferedItemPosition getAnySubItemPosition() {
		BufferedItemPosition result = (BufferedItemPosition) super.getAnySubItemPosition();
		if (result != null) {
			result.parentItemPosition = standardItemPosition;
			result.standardItemPosition = new ItemPosition(standardItemPosition, null,
					result.containingListFieldIfNotRoot, result.containingListSize, result.index);
			result.bufferedParentItemPosition = this;
			result.bufferedItem = null;
		}
		return result;
	}

	@Override
	public List<BufferedItemPosition> getSubItemPositions() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (ItemPosition standardSubItemPosition : standardItemPosition.getSubItemPositions()) {
			result.add(new BufferedItemPosition(standardSubItemPosition));
		}
		return result;
	}

	@Override
	public String toString() {
		return "Buffered" + super.toString();
	}

}