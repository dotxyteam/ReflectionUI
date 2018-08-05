package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;

public interface IListTypeInfo extends ITypeInfo {
	ITypeInfo getItemType();

	Object[] toArray(Object listValue);

	boolean canInstanciateFromArray();

	Object fromArray(Object[] array);

	boolean canReplaceContent();

	void replaceContent(Object listValue, Object[] array);

	IListStructuralInfo getStructuralInfo();

	IListItemDetailsAccessMode getDetailsAccessMode();

	boolean isOrdered();

	boolean isInsertionAllowed();

	boolean isRemovalAllowed();

	boolean canViewItemDetails();

	List<AbstractListAction> getDynamicActions(List<? extends ItemPosition> selection, Object rootListValue);

	List<AbstractListProperty> getDynamicProperties(List<? extends ItemPosition> selection, Object rootListValue);

	List<IMethodInfo> getAdditionalItemConstructors(Object listValue);

	boolean isItemNullValueDistinct();

	ValueReturnMode getItemReturnMode();

	boolean isItemConstructorSelectable();

}
