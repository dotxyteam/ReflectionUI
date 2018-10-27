package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;

/**
 * Allows to describe tabular and hierarchical preferences of list types.
 * 
 * @author olitank
 *
 */
public interface IListStructuralInfo {

	List<IColumnInfo> getColumns();

	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	IInfoFilter getItemInfoFilter(ItemPosition itemPosition);

	int getLength();

}
