package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Map;

import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public class ColumnInfoProxy implements IColumnInfo {
	protected IColumnInfo base;

	public ColumnInfoProxy(IColumnInfo base) {
		super();
		this.base = base;
	}

	public String getCaption() {
		return base.getCaption();
	}

	public boolean hasCellValue(ItemPosition itemPosition) {
		return base.hasCellValue(itemPosition);
	}

	public String getCellValue(ItemPosition itemPosition) {
		return base.getCellValue(itemPosition);
	}

	public String getName() {
		return base.getName();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
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
		ColumnInfoProxy other = (ColumnInfoProxy) obj;
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