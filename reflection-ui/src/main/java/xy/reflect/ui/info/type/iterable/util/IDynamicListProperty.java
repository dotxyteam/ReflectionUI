package xy.reflect.ui.info.type.iterable.util;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class allows to describe a property of a list instance that can be
 * accessed according to a given selection of items.
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

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
