


package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;

/**
 * List structural information proxy class. The methods in this class should be overriden to
 * provide custom information.
 * 
 * @author olitank
 *
 */
public class ListStructuralInfoProxy implements IListStructuralInfo {

	protected IListStructuralInfo base;

	public ListStructuralInfoProxy(IListStructuralInfo base) {
		this.base = base;
	}

	public int getLength() {
		return base.getLength();
	}

	public List<IColumnInfo> getColumns() {
		return base.getColumns();
	}

	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		return base.getItemSubListField(itemPosition);
	}

	public IInfoFilter getItemInfoFilter(ItemPosition itemPosition) {
		return base.getItemInfoFilter(itemPosition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListStructuralInfoProxy other = (ListStructuralInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return base.toString();
	}

}
