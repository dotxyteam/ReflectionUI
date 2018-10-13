package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public interface IListProperty extends IFieldInfo {

	Object getRootListValue();

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
