package xy.reflect.ui.info.type.iterable.util;

import java.util.List;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class allows to describe an action that can be performed on a list
 * instance according to a given selection of items.
 * 
 * @author olitank
 *
 */
public interface IDynamicListAction extends IMethodInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
