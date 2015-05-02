package xy.reflect.ui.undo;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;

public class SetListValueModification implements
		IModification {
	protected Object[] listValue;
	protected Object listOwner;
	protected IFieldInfo listField;
	protected ReflectionUI reflectionUI;

	public SetListValueModification(ReflectionUI reflectionUI,
			Object[] listValue, Object listOwner, IFieldInfo listField) {
		this.reflectionUI = reflectionUI;
		this.listValue = listValue;
		this.listOwner = listOwner;
		this.listField = listField;
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public IModification applyAndGetOpposite(boolean refreshView) {
		IListTypeInfo listType = (IListTypeInfo) listField.getType();
		Object[] lastListValue = listType.toListValue(listField
				.getValue(listOwner));

		Object listFieldValue = listType.fromListValue(listValue);
		listField.setValue(listOwner, listFieldValue);

		final SetListValueModification currentModif = this;
		return new SetListValueModification(reflectionUI, lastListValue,
				listOwner, listField) {
			@Override
			public String getTitle() {
				return ModificationStack.getUndoTitle(currentModif
						.getTitle());
			}
		};
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Edit '" + listField.getCaption() + "' of '"
				+ reflectionUI.getObjectKind(listOwner) + "'";
	}

}

