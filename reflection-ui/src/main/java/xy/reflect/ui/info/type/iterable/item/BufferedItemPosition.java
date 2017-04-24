package xy.reflect.ui.info.type.iterable.item;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;

public class BufferedItemPosition extends ItemPosition {

	public static final Object NULL_FAKE_ITEM = new Object() {
		@Override
		public String toString() {
			return "NULL_BUFFERED_ITEM_REMEMBERED";
		}

	};
	protected Object fakeItem;
	protected Object[] bufferedSubListRawValue;
	protected IFieldInfo bufferedSubListField;

	protected BufferedItemPosition() {
		super();
	}

	@Override
	public Object getItem() {
		if (fakeItem != null) {
			if (fakeItem == NULL_FAKE_ITEM) {
				return null;
			} else {
				return fakeItem;
			}
		}
		return super.getItem();
	}

	public void setFakeItem(Object item) {
		if (item == null) {
			fakeItem = NULL_FAKE_ITEM;
		} else {
			fakeItem = item;
		}
	}

	public void unsetFakeItem() {
		fakeItem = null;
	}

	public void refreshBranch() {
		if (isRoot()) {
			((BufferedItemPositionFactory) factory).refresh();
		}
		bufferedSubListRawValue = null;
		fakeItem = null;
		bufferedSubListField = null;
	}

	@Override
	public BufferedItemPosition getParentItemPosition() {
		return (BufferedItemPosition) parentItemPosition;
	}

	@Override
	public BufferedItemPosition getRoot() {
		return (BufferedItemPosition) super.getRoot();
	}

	@Override
	public BufferedItemPosition getSibling(int index2) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSibling(index2);
		result.fakeItem = null;
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
	public IFieldInfo getSubListField() {
		if (bufferedSubListField == null) {
			bufferedSubListField = super.getSubListField();
		}
		return bufferedSubListField;
	}

	@Override
	public Object[] retrieveSubListRawValue() {
		if (bufferedSubListRawValue == null) {
			bufferedSubListRawValue = super.retrieveSubListRawValue();
		}
		return bufferedSubListRawValue;
	}

	@Override
	public BufferedItemPosition getSubItemPosition(int index) {
		BufferedItemPosition result = (BufferedItemPosition) super.getSubItemPosition(index);
		if (result != null) {
			result.bufferedSubListField = null;
			result.bufferedSubListRawValue = null;
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