package xy.reflect.ui.info.type.iterable.structure.column;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public interface IColumnInfo extends IInfo{

	String getCellValue(ItemPosition itemPosition, Object rootListValue);

	boolean hasCellValue(ItemPosition itemPosition, Object rootListValue);

	int getMinimalCharacterCount();

}