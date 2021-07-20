


package xy.reflect.ui.info.type.iterable.util;

import java.util.List;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class allows to specify an action that can be performed on a list
 * instance according to the current selection of items. Such an action will
 * typically be available on the list control tool bar.
 * 
 * The current selection is provided as the 1st parameter of
 * {@link IListTypeInfo#getDynamicActions(List, xy.reflect.ui.util.Mapper)}.
 * 
 * Note that the owner object passed to
 * {@link #invoke(Object, xy.reflect.ui.info.method.InvocationData)} is
 * {@link IDynamicListAction#NO_OWNER}.
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

	/**
	 * @return the list of item positions that should be selected after the
	 *         execution of the current action or null if the selection should not
	 *         be updated.
	 */
	List<ItemPosition> getPostSelection();

}
