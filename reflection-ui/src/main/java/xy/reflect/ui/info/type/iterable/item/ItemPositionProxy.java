


package xy.reflect.ui.info.type.iterable.item;

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

/**
 * Item position proxy class. The methods in this class should be overriden to
 * provide custom information.
 * 
 * @author olitank
 *
 */
public class ItemPositionProxy extends ItemPosition {

	protected ItemPosition base;

	public ItemPositionProxy(ItemPosition base) {
		this.base = base;
	}

	public ItemPosition getBase() {
		return base;
	}

	public void setBase(ItemPosition base) {
		this.base = base;
	}

	public boolean supportsItem(Object object) {
		return base.supportsItem(object);
	}

	public int getIndex() {
		return base.getIndex();
	}

	public Object getItem() {
		return base.getItem();
	}

	public String getContainingListTitle() {
		return base.getContainingListTitle();
	}

	public IListTypeInfo getContainingListType() {
		return base.getContainingListType();
	}

	public ItemPosition getParentItemPosition() {
		return base.getParentItemPosition();
	}

	public String getPath() {
		return base.getPath();
	}

	public int getDepth() {
		return base.getDepth();
	}

	public List<? extends ItemPosition> getPreviousSiblings() {
		return base.getPreviousSiblings();
	}

	public List<? extends ItemPosition> getFollowingSiblings() {
		return base.getFollowingSiblings();
	}

	public List<ItemPosition> getAncestors() {
		return base.getAncestors();
	}

	public ItemPosition getSibling(int index2) {
		return base.getSibling(index2);
	}

	public IFieldInfo getContainingListFieldIfNotRoot() {
		return base.getContainingListFieldIfNotRoot();
	}

	public int getContainingListSize() {
		return base.getContainingListSize();
	}

	public ValueReturnMode geContainingListReturnMode() {
		return base.geContainingListReturnMode();
	}

	public boolean isContainingListGetOnly() {
		return base.isContainingListGetOnly();
	}

	public List<? extends ItemPosition> getSubItemPositions() {
		return base.getSubItemPositions();
	}

	public boolean isRoot() {
		return base.isRoot();
	}

	public ItemPosition getRoot() {
		return base.getRoot();
	}

	public ValueReturnMode getItemReturnMode() {
		return base.getItemReturnMode();
	}

	public Object[] retrieveSubListRawValue() {
		return base.retrieveSubListRawValue();
	}

	public IFieldInfo getSubListField() {
		return base.getSubListField();
	}

	public ItemPosition getSubItemPosition(int index) {
		return base.getSubItemPosition(index);
	}

	public ItemPosition clone() {
		return base.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		ItemPositionProxy other = (ItemPositionProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ItemPositionProxy [delegate=" + base + "]";
	}

}
