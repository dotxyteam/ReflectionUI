package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;

public interface IListAction extends IMethodInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

	List<ItemPosition> getPostSelection();

	boolean isEnabled();

}
