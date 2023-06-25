


package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

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
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	IColumnInfo NULL_COLUMN_INFO = new IColumnInfo() {
		
		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return "NULL_COLUMN_INFO";
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean hasCellValue(ItemPosition itemPosition) {
			return false;
		}

		@Override
		public String getCellValue(ItemPosition itemPosition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getMinimalCharacterCount() {
			return 30;
		}

		
		@Override
		public String toString() {
			return "NULL_COLUMN_INFO";
		}
	};

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
