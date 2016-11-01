package xy.reflect.ui.undo;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class UpdateListValueModification implements IModification {

	protected ItemPosition itemPosition;
	protected Object[] newListRawValue;
	protected ReflectionUI reflectionUI;

	public UpdateListValueModification(ReflectionUI reflectionUI, ItemPosition itemPosition, Object[] newListRawValue) {
		this.reflectionUI = reflectionUI;
		this.itemPosition = itemPosition;
		this.newListRawValue = newListRawValue;
	}

	public static boolean isCompatibleWith(ItemPosition itemPosition) {
		ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			if (!isCompatibleWith(parentItemPosition)) {
				return false;
			}
		}
		IListTypeInfo containingListType = itemPosition.getContainingListType();
		IFieldInfo containingListField = itemPosition.getContainingListField();
		if (containingListField.isGetOnly()) {
			if (containingListType.canReplaceContent()
					&& (containingListField.getValueReturnMode() == ValueReturnMode.SELF)) {
				return true;
			}
		} else {
			if (containingListType.canInstanciateFromArray() || containingListType.canReplaceContent()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IModification applyAndGetOpposite() {
		Object[] oldListRawValue = itemPosition.getContainingListRawValue();
		updateListValueRecursively(itemPosition, newListRawValue);
		return new UpdateListValueModification(reflectionUI, itemPosition, oldListRawValue);
	}

	protected void updateListValueRecursively(ItemPosition itemPosition, Object[] listRawValue) {
		if (!isCompatibleWith(itemPosition)) {
			return;
		}
		Object listOwner = itemPosition.getContainingListOwner();
		IFieldInfo listField = itemPosition.getContainingListField();

		if (!renewListValue(reflectionUI, listRawValue, listOwner, listField)) {
			if (!replaceListValueContent(reflectionUI, listRawValue, listOwner, listField)) {
				throw new ReflectionUIError();
			}
		}
		ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object[] parentListRawValue = parentItemPosition.getContainingListRawValue();
			parentListRawValue[parentItemPosition.getIndex()] = listOwner;
			updateListValueRecursively(parentItemPosition, parentListRawValue);
		}
	}

	protected boolean renewListValue(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
			IFieldInfo listField) {
		IListTypeInfo listType = (IListTypeInfo) listField.getType();
		if (!listType.canInstanciateFromArray()) {
			return false;
		}
		if (listField.isGetOnly()) {
			return false;
		}
		Object listValue = listType.fromArray(listRawValue);
		listField.setValue(listOwner, listValue);
		return true;
	}

	protected boolean replaceListValueContent(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
			IFieldInfo listField) {
		IListTypeInfo listType = (IListTypeInfo) listField.getType();
		if (!listType.canReplaceContent()) {
			return false;
		}
		if ((listField.getValueReturnMode() != ValueReturnMode.SELF) && listField.isGetOnly()) {
			return false;
		}
		Object listValue = listField.getValue(listOwner);
		listType.replaceContent(listValue, listRawValue);
		if (!listField.isGetOnly()) {
			listField.setValue(listOwner, listValue);
		}
		return true;
	}

	@Override
	public boolean isNull() {
		return false;
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