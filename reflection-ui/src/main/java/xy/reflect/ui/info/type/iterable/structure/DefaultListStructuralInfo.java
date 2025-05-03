
package xy.reflect.ui.info.type.iterable.structure;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.structure.column.StringValueColumnInfo;

/**
 * Default implementation of {@link IListStructuralInfo}. It specifies a simple
 * (not hierarchical) list with a unique column that displays the string
 * representation of each item.
 * 
 * @author olitank
 *
 */
public class DefaultListStructuralInfo implements IListStructuralInfo {

	protected ReflectionUI reflectionUI;

	public DefaultListStructuralInfo(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public int getWidth() {
		return -1;
	}

	@Override
	public int getHeight() {
		return -1;
	}

	@Override
	public List<IColumnInfo> getColumns() {
		return Collections.<IColumnInfo>singletonList(new StringValueColumnInfo(reflectionUI));
	}

	@Override
	public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
		return null;
	}

	@Override
	public IInfoFilter getItemInfoFilter(ItemPosition itemPosition) {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		DefaultListStructuralInfo other = (DefaultListStructuralInfo) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultListStructuralInfo [reflectionUI=" + reflectionUI + "]";
	}

}
