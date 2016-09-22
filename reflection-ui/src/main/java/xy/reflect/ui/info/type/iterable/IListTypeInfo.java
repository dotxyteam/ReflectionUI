package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public interface IListTypeInfo extends ITypeInfo {
	ITypeInfo getItemType();

	Object[] toArray(Object listValue);

	boolean canInstanciateFromArray();

	Object fromArray(Object[] array);

	boolean canReplaceContent();

	void replaceContent(Object listValue, Object[] array);

	IListStructuralInfo getStructuralInfo();

	boolean isOrdered();
	boolean canAdd();
	boolean canRemove();
	boolean canViewItemDetails();

	List<AbstractListAction> getSpecificActions(Object object, IFieldInfo field,
			List<? extends ItemPosition> selection);

	List<IMethodInfo> getObjectSpecificItemConstructors(Object object, IFieldInfo field);

}
