package xy.reflect.ui.undo;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public class UpdateListValueModification implements IModification {

	protected ItemPosition itemPosition;
	protected Object[] newListRawValue;
	protected ReflectionUI reflectionUI;
	private Object[] oldListRawValue;

	public UpdateListValueModification(ReflectionUI reflectionUI, ItemPosition itemPosition, Object[] oldListRawValue,
			Object[] newListRawValue) {
		this.reflectionUI = reflectionUI;
		this.itemPosition = itemPosition;
		this.newListRawValue = newListRawValue;
		this.oldListRawValue = oldListRawValue;
	}

	public UpdateListValueModification(ReflectionUI reflectionUI, ItemPosition itemPosition, Object[] newListRawValue) {
		this(reflectionUI, itemPosition, itemPosition.getContainingListRawValue(), newListRawValue);
	}

	public static boolean isContainingListItemsLocked(ItemPosition itemPosition) {
		IListTypeInfo containingListType = itemPosition.getContainingListType();
		if (!containingListType.canReplaceContent()) {
			if (!(containingListType.canInstanciateFromArray() && !itemPosition.getContainingListField().isGetOnly())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IModification applyAndGetOpposite() {
		updateListValueRecursively(itemPosition, newListRawValue);
		return new UpdateListValueModification(reflectionUI, itemPosition, oldListRawValue);
	}

	protected void updateListValueRecursively(ItemPosition itemPosition, Object[] listRawValue) {
		if (!isContainingListItemsLocked(itemPosition)) {
			Object listOwner = itemPosition.getContainingListOwner();
			IFieldInfo listField = itemPosition.getContainingListField();
			if (itemPosition.getContainingListType().canReplaceContent()) {
				replaceListValueContent(reflectionUI, listRawValue, listOwner, listField);
			} else {
				setListValue(reflectionUI, listRawValue, listOwner, listField);
			}
		}
		ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object[] parentListRawValue = parentItemPosition.getContainingListRawValue();
			parentListRawValue[parentItemPosition.getIndex()] = parentItemPosition.getItem();
			updateListValueRecursively(parentItemPosition, parentListRawValue);
		}
	}

	protected void setListValue(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
			IFieldInfo listField) {
		IListTypeInfo listType = (IListTypeInfo) listField.getType();
		Object listValue = listField.getValue(listOwner);
		listValue = listType.fromArray(listRawValue);
		listField.setValue(listOwner, listValue);
	}

	protected void replaceListValueContent(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
			IFieldInfo listField) {
		IListTypeInfo listType = (IListTypeInfo) listField.getType();
		Object listValue = listField.getValue(listOwner);
		listType.replaceContent(listValue, listRawValue);
		if (!listField.isGetOnly()) {
			listField.setValue(listOwner, listValue);
		}
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public String getTitle() {
		return "Edit " + itemPosition.getContainingListPath();
	}

	@Override
	public IInfo getTarget() {
		return itemPosition.getRootListItemPosition().getContainingListField();
	}

}