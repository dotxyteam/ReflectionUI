package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;

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

	List<IListAction> getDynamicActions(List<? extends ItemPosition> selection, Object rootListValue);

	List<IListProperty> getDynamicProperties(List<? extends ItemPosition> selection, Object rootListValue);

	boolean isItemNullValueDistinct();

	ValueReturnMode getItemReturnMode();

	boolean isItemConstructorSelectable();

	void onSelection(List<? extends ItemPosition> newSelection, Object rootListValue);

}
