
package xy.reflect.ui.info.type.iterable.structure;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPositionFactory;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;

/**
 * Allows to describe tabular and hierarchical preferences about list types.
 * 
 * @author olitank
 *
 */
public interface IListStructuralInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	IListStructuralInfo NULL_LIST_STRUCTURAL_INFO = new IListStructuralInfo() {

		@Override
		public String toString() {
			return "NULL_LIST_STRUCTURAL_INFO";
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
			return Collections.singletonList(IColumnInfo.NULL_COLUMN_INFO);
		}

		@Override
		public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
			return null;
		}

		@Override
		public IInfoFilter getItemDetailsInfoFilter(ItemPosition itemPosition) {
			return IInfoFilter.DEFAULT;
		}

	};

	/**
	 * Key to access the {@link ItemPositionFactory#getSource()} value (which is
	 * generally the list control) of a tree item from its details. The value is to
	 * be retrieved from the {@link IFieldInfo#getSpecificProperties()} map of a
	 * field that accesses a sub-list value from the item. It typically allows to
	 * detect that a 'slave' list control displays items that are already displayed
	 * by a 'master' list control.
	 */
	String SUB_LIST_FIELD_CONTROL_MASTER_KEY = IListStructuralInfo.class.getName()
			+ ".SUB_LIST_FIELD_CONTROL_MASTER_KEY";

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
	IInfoFilter getItemDetailsInfoFilter(ItemPosition itemPosition);

	/**
	 * @return the height (in pixels) of the list control or -1 if the default
	 *         height should be used. Note that when the list is actually a tree,
	 *         only the return value from the root list type information should be
	 *         considered.
	 */
	int getHeight();

	/**
	 * @return the width (in pixels) of the list control or -1 if the default width
	 *         should be used. Note that when the list is actually a tree, only the
	 *         return value from the root list type information should be
	 *         considered.
	 */
	int getWidth();

}
