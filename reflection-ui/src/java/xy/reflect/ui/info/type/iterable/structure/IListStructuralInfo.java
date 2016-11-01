package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;


public interface IListStructuralInfo {

	List<IColumnInfo> getColumns();

	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	IInfoCollectionSettings getItemInfoSettings(
			ItemPosition itemPosition);

}
