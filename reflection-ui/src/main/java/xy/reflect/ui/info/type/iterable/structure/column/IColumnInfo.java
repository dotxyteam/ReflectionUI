package xy.reflect.ui.info.type.iterable.structure.column;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public interface IColumnInfo extends IInfo{

	String getCellValue(ItemPosition itemPosition);

}