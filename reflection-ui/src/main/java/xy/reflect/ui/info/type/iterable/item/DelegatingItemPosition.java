package xy.reflect.ui.info.type.iterable.item;

import java.util.List;

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

	public Object getItem(Object rootListValue) {
		return delegate.getItem(rootListValue);
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

	public List<? extends ItemPosition> getPreviousSiblings(Object rootListValue) {
		return delegate.getPreviousSiblings(rootListValue);
	}

	public List<? extends ItemPosition> getFollowingSiblings(Object rootListValue) {
		return delegate.getFollowingSiblings(rootListValue);
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

	public int getContainingListSize(Object rootListValue) {
		return delegate.getContainingListSize(rootListValue);
	}

	public ValueReturnMode geContainingListReturnMode() {
		return delegate.geContainingListReturnMode();
	}

	public boolean isContainingListGetOnly() {
		return delegate.isContainingListGetOnly();
	}

	public List<? extends ItemPosition> getSubItemPositions(Object rootListValue) {
		return delegate.getSubItemPositions(rootListValue);
	}

	public boolean isRoot() {
		return delegate.isRoot();
	}

	public ItemPosition getRoot() {
		return delegate.getRoot();
	}

	public ValueReturnMode getItemReturnMode() {
		return delegate.getItemReturnMode();
	}

	public Object[] retrieveSubListRawValue(Object rootListValue) {
		return delegate.retrieveSubListRawValue(rootListValue);
	}

	public IFieldInfo getSubListField(Object rootListValue) {
		return delegate.getSubListField(rootListValue);
	}

	public ItemPosition getSubItemPosition(int index, Object rootListValue) {
		return delegate.getSubItemPosition(index, rootListValue);
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
