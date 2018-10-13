package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public interface IListAction extends IMethodInfo {

	Object getRootListValue();

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
