package xy.reflect.ui.info.type.iterable.util.structure;

import java.awt.Image;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public class ListStructuralInfoProxy implements IListStructuralInfo{

	protected IListStructuralInfo base;

	public ListStructuralInfoProxy(IListStructuralInfo base) {
		this.base = base;
	}

	public String getCellValue(ItemPosition itemPosition, int columnIndex) {
		return base.getCellValue(itemPosition, columnIndex);
	}

	public Image getCellIconImage(ItemPosition itemPosition, int columnIndex) {
		return base.getCellIconImage(itemPosition, columnIndex);
	}

	public String getColumnCaption(int columnIndex) {
		return base.getColumnCaption(columnIndex);
	}

	public int getColumnCount() {
		return base.getColumnCount();
	}

	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		return base.getItemSubListField(itemPosition);
	}

	public IInfoCollectionSettings getItemInfoSettings(ItemPosition itemPosition) {
		return base.getItemInfoSettings(itemPosition);
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
