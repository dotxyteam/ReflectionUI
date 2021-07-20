


package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;

/**
 * Allows to describe tabular and hierarchical preferences about list types.
 * 
 * @author olitank
 *
 */
public interface IListStructuralInfo {

	/**
	 * @return the list of columns. Note that when the list is actually a tree then
	 *         this method must be called from the root list type information.
	 */
	List<IColumnInfo> getColumns();

	/**
	 * @param itemPosition The position of the item in the list/tree.
	 * @return the field used to get the sub-list value from the current item or
	 *         null if there is no sub-list.
	 */
	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	/**
	 * @param itemPosition The position of the item in the list/tree.
	 * @return the filter that must be applied to the item type information or null.
	 */
	IInfoFilter getItemInfoFilter(ItemPosition itemPosition);

	/**
	 * @return the height (in pixels) of the list or -1 if the default height should
	 *         be used. Note that when the list is actually a tree then this method
	 *         must be called from the root list type information.
	 */
	int getLength();

	
}
