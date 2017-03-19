package xy.reflect.ui.info.type.iterable.item;

import java.util.List;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

public class DelegatingItemPosition extends ItemPosition {

	protected ItemPosition delegate;

	public DelegatingItemPosition(ItemPosition delegate) {
		super(null, null, -1);
		this.delegate = delegate;
	}

	public ItemPosition getDelegate() {
		return delegate;
	}

	public void setDelegate(ItemPosition delegate) {
		this.delegate = delegate;
	}

	public boolean supportsItem(Object object) {
		return delegate.supportsItem(object);
	}

	public int getIndex() {
		return delegate.getIndex();
	}

	public Object getItem() {
		return delegate.getItem();
	}

	public String getContainingListTitle() {
		return delegate.getContainingListTitle();
	}

	public IFieldControlData getContainingListData() {
		return delegate.getContainingListData();
	}

	public Object[] getContainingListRawValue() {
		return delegate.getContainingListRawValue();
	}

	public IListTypeInfo getContainingListType() {
		return delegate.getContainingListType();
	}

	public ItemPosition getParentItemPosition() {
		return delegate.getParentItemPosition();
	}

	public int getDepth() {
		return delegate.getDepth();
	}

	public List<? extends ItemPosition> getPreviousSiblings() {
		return delegate.getPreviousSiblings();
	}

	public List<? extends ItemPosition> getFollowingSiblings() {
		return delegate.getFollowingSiblings();
	}

	public List<ItemPosition> getAncestors() {
		return delegate.getAncestors();
	}

	public ItemPosition getSibling(int index2) {
		return delegate.getSibling(index2);
	}

	public IFieldControlData getSubListData() {
		return delegate.getSubListData();
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		return delegate.getSubItemPositions();
	}

	public boolean isRootListItemPosition() {
		return delegate.isRootListItemPosition();
	}

	public ItemPosition getRootListItemPosition() {
		return delegate.getRootListItemPosition();
	}

	public IFieldControlData getRootListData() {
		return delegate.getRootListData();
	}

	public ValueReturnMode getItemReturnMode() {
		return delegate.getItemReturnMode();
	}

	public ItemPosition clone() {
		return delegate.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		DelegatingItemPosition other = (DelegatingItemPosition) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingItemPosition [delegate=" + delegate + "]";
	}
	
	

}
