
package xy.reflect.ui.info.type.iterable.structure;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
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

		@Override
		public boolean isSlave(Object subFormObject, IFieldInfo subFormField, BufferedItemPosition itemPosition) {
			return false;
		}

	};

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

	/**
	 * 
	 * 
	 * @param subFormObject A descendant form object.
	 * @param subFormField  A descendant form field.
	 * @param itemPosition  The position of the item in the current list/tree.
	 * @return whether the list/tree control represented by the given sub-form
	 *         object and field is a slave of the current list/tree control. Indeed
	 *         a list control (actually, a tree control) may have a master-slave
	 *         relationship with certain descendant list/tree controls that appear
	 *         in its embedded detail view. They display sublist items already
	 *         displayed in the master list/tree control, and their behavior is
	 *         adapted accordingly. Typically, trying to open the details view of a
	 *         sublist item from the slave control will result in selecting this
	 *         item in the master control that will provide the item details view.
	 */
	boolean isSlave(Object subFormObject, IFieldInfo subFormField, BufferedItemPosition itemPosition);

}
