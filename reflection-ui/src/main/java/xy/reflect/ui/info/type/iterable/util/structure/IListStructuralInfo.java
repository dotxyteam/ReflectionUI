package xy.reflect.ui.info.type.iterable.util.structure;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;


public interface IListStructuralInfo {

	String getCellValue(ItemPosition itemPosition, int columnIndex);

	String getColumnCaption(int columnIndex);

	int getColumnCount();

	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	IInfoCollectionSettings getItemInfoSettings(
			ItemPosition itemPosition);
}
