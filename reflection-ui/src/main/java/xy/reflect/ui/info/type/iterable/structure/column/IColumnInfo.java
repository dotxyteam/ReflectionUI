


package xy.reflect.ui.info.type.iterable.structure.column;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * Allows to describe {@link IListTypeInfo} columns.
 * 
 * @author olitank
 *
 */
public interface IColumnInfo extends IInfo {

	/**
	 * @param itemPosition The position of the item in the list/tree.
	 * @return the value that must be displayed in the current cell.
	 */
	String getCellValue(ItemPosition itemPosition);

	/**
	 * @param itemPosition The position of the item in the list/tree.
	 * @return whether a value should be displayed in the current cell or not. If
	 *         false is returned then {@link #getCellValue(ItemPosition)} should not
	 *         be called.
	 */
	boolean hasCellValue(ItemPosition itemPosition);

	/**
	 * @return the number of characters that the column should always be able to
	 *         display.
	 */
	int getMinimalCharacterCount();

}
