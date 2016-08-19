package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;

public interface IListTypeInfo extends ITypeInfo {
	ITypeInfo getItemType();

	Object[] toArray(Object listValue);

	Object fromArray(Object[] array);

	IListStructuralInfo getStructuralInfo();

	boolean isOrdered();

	List<AbstractListAction> getSpecificActions(Object object, IFieldInfo field,
			List<? extends ItemPosition> selection);

	List<IMethodInfo> getObjectSpecificItemConstructors(Object object, IFieldInfo field);

	boolean canInstanciateFromArray();


}
