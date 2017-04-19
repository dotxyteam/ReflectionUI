package xy.reflect.ui.info.type.iterable.item;

import java.util.List;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;

public class DelegatingItemPosition extends ItemPosition {

	protected ItemPosition delegate;

	public DelegatingItemPosition(ItemPosition delegate) {
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

	public IListTypeInfo getContainingListType() {
		return delegate.getContainingListType();
	}

	public ItemPosition getParentItemPosition() {
		return delegate.getParentItemPosition();
	}

	public String getPath() {
		return delegate.getPath();
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

	public IFieldInfo getContainingListFieldIfNotRoot() {
		return delegate.getContainingListFieldIfNotRoot();
	}

	public int getContainingListSize() {
		return delegate.getContainingListSize();
	}

	public ValueReturnMode geContainingListReturnMode() {
		return delegate.geContainingListReturnMode();
	}

	public boolean isContainingListGetOnly() {
		return delegate.isContainingListGetOnly();
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		return delegate.getSubItemPositions();
	}

	public boolean isRoot() {
		return delegate.isRoot();
	}

	public ItemPosition getRoot() {
		return delegate.getRoot();
	}

	public IFieldControlData getRootListData() {
		return delegate.getRootListData();
	}

	public ValueReturnMode getItemReturnMode() {
		return delegate.getItemReturnMode();
	}

	public Object[] retrieveSubListRawValue() {
		return delegate.retrieveSubListRawValue();
	}

	public IFieldInfo getSubListField() {
		return delegate.getSubListField();
	}

	public ItemPosition getSubItemPosition(int index) {
		return delegate.getSubItemPosition(index);
	}

	public IListStructuralInfo getStructuralInfo() {
		return delegate.getStructuralInfo();
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
