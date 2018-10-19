package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;

public interface IListProperty extends IFieldInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
