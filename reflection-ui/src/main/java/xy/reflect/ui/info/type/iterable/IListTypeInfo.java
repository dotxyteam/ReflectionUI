package xy.reflect.ui.info.type.iterable;

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;

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

	List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	boolean isItemNullValueDistinct();

	ValueReturnMode getItemReturnMode();

	boolean isItemConstructorSelectable();

	void onSelection(List<? extends ItemPosition> newSelection);

}
