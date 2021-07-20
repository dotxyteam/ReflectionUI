


package xy.reflect.ui.info.type.iterable.util;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class allows to specify a list instance property that can be accessed
 * according to the current selection of items. Such a property will typically
 * be available on the list control tool bar.
 * 
 * The current selection is provided as the 1st parameter of
 * {@link IListTypeInfo#getDynamicProperties(List, xy.reflect.ui.util.Mapper)}.
 * 
 * Note that the owner object passed to {@link #setValue(Object, Object)} and
 * {@link #getValue(Object)} is {@link IDynamicListProperty#NO_OWNER}.
 * 
 * @author olitank
 *
 */
public interface IDynamicListProperty extends IFieldInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

	/**
	 * @return the list of item positions that should be selected after the display
	 *         of the current property or null if the selection should not be
	 *         updated.
	 */
	List<ItemPosition> getPostSelection();

	/**
	 * @return whether the list property can be displayed or not.
	 */
	boolean isEnabled();

}
