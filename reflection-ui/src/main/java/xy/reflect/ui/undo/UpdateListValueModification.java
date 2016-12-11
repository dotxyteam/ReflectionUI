package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class UpdateListValueModification extends AbstractModification {

	protected ItemPosition itemPosition;
	protected Object[] newListRawValue;

	protected List<IModification> undoModifications;

	public UpdateListValueModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
		super(target);
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
		IControlData containingListData = itemPosition.getContainingListData();
		if (containingListData.isGetOnly()) {
			if (containingListType.canReplaceContent()
					&& (containingListData.getValueReturnMode() == ValueReturnMode.SELF)) {
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
	public String getTitle() {
		return ControlDataValueModification.getTitle(target);
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				undoModifications = new ArrayList<IModification>();
				updateListValueRecursively(itemPosition, newListRawValue);
			}

		};
	}

	@Override
	protected Runnable createUndoJob() {
		return new Runnable() {
			@Override
			public void run() {
				new CompositeModification(target, null, UndoOrder.getDefault(), undoModifications)
						.applyAndGetOpposite();
				undoModifications = null;
			}

		};
	}

	protected void updateListValueRecursively(ItemPosition itemPosition, Object[] listRawValue) {
		if (!isCompatibleWith(itemPosition)) {
			return;
		}
		if (!renewListValue(listRawValue, itemPosition)) {
			if (!changeListValueContent(listRawValue, itemPosition)) {
				throw new ReflectionUIError();
			}
		}
		ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object[] parentListRawValue = parentItemPosition.getContainingListRawValue();
			updateListValueRecursively(parentItemPosition, parentListRawValue);
		}
	}

	protected boolean renewListValue(Object[] listRawValue, ItemPosition itemPosition) {
		IListTypeInfo listType = itemPosition.getContainingListType();
		if (!listType.canInstanciateFromArray()) {
			return false;
		}
		if (itemPosition.getContainingListData().isGetOnly()) {
			return false;
		}
		Object listValue = listType.fromArray(listRawValue);
		setListValue(itemPosition.getContainingListData(), listValue);
		return true;
	}

	protected boolean changeListValueContent(Object[] listRawValue, ItemPosition itemPosition) {
		IListTypeInfo listType = itemPosition.getContainingListType();
		if (!listType.canReplaceContent()) {
			return false;
		}
		IControlData listData = itemPosition.getContainingListData();
		if ((listData.getValueReturnMode() != ValueReturnMode.SELF) && listData.isGetOnly()) {
			return false;
		}
		Object listValue = listData.getValue();
		undoModifications.add(0,
				new ChangeListValueContentModification(listData, listRawValue, target).applyAndGetOpposite());

		if (!listData.isGetOnly()) {
			setListValue(listData, listValue);
		}
		return true;
	}

	private void setListValue(IControlData listData, Object listValue) {
		undoModifications.add(0, new ControlDataValueModification(listData, listValue, target).applyAndGetOpposite());
	}

	public static class ChangeListValueContentModification extends AbstractModification {

		protected IControlData listData;
		protected Object[] listRawValue;

		public ChangeListValueContentModification(IControlData listData, Object[] listRawValue, IInfo target) {
			super(target);
			this.listData = listData;
			this.listRawValue = listRawValue;
		}

		@Override
		public String getTitle() {
			return ControlDataValueModification.getTitle(target);
		}

		@Override
		protected Runnable createDoJob() {
			return new Runnable() {

				@Override
				public void run() {
					IListTypeInfo listType = (IListTypeInfo) listData.getType();
					Object listValue = listData.getValue();
					listType.replaceContent(listValue, listRawValue);
				}
			};
		}

		@Override
		protected Runnable createUndoJob() {
			final IListTypeInfo listType = (IListTypeInfo) listData.getType();
			Object oldListValue = listData.getValue();
			final Object[] oldListRawValue = listType.toArray(oldListValue);
			return new Runnable() {

				@Override
				public void run() {
					Object listValue = listData.getValue();
					listType.replaceContent(listValue, oldListRawValue);
				}
			};
		}

	}
}