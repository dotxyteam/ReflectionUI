package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.control.input.ControlDataProxy;
import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
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
		IControlData containingListData = itemPosition.getContainingListData();
		IListTypeInfo containingListType = itemPosition.getContainingListType();
		if (containingListType.canReplaceContent()) {
			if (containingListData.getValueReturnMode() == ValueReturnMode.SELF) {
				return true;
			}
			if (!containingListData.isGetOnly()) {
				return true;
			}
		}
		if (containingListType.canInstanciateFromArray()) {
			if (!containingListData.isGetOnly()) {
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

		updateListValue(itemPosition, listRawValue);

		ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object[] parentListRawValue = parentItemPosition.getContainingListRawValue();
			parentListRawValue[parentItemPosition.getIndex()] = parentItemPosition.getLastKnownItem();
			updateListValueRecursively(parentItemPosition, parentListRawValue);
		}
	}

	protected void updateListValue(ItemPosition itemPosition, Object[] listRawValue) {
		IControlData listData = itemPosition.getContainingListData();
		IListTypeInfo listType = itemPosition.getContainingListType();
		if (listType.canReplaceContent()) {
			if (listData.getValueReturnMode() == ValueReturnMode.SELF) {
				final Object listValue = listData.getValue();
				undoModifications.add(0, new ChangeListContentModification(new ControlDataProxy(listData) {
					@Override
					public Object getValue() {
						return listValue;
					}
				}, listRawValue, target).applyAndGetOpposite());
				return;
			}
			if (!listData.isGetOnly()) {
				final Object listValue = listData.getValue();
				undoModifications.add(0, new ChangeListContentModification(new ControlDataProxy(listData) {
					@Override
					public Object getValue() {
						return listValue;
					}
				}, listRawValue, target).applyAndGetOpposite());
				undoModifications.add(0,
						new ControlDataValueModification(itemPosition.getContainingListData(), listValue, target)
								.applyAndGetOpposite());
				return;
			}
		}
		if (listType.canInstanciateFromArray()) {
			if (!listData.isGetOnly()) {
				Object listValue = listType.fromArray(listRawValue);
				undoModifications.add(0,
						new ControlDataValueModification(itemPosition.getContainingListData(), listValue, target)
								.applyAndGetOpposite());
				return;
			}
		}
		throw new ReflectionUIError();
	}

	public static class ChangeListContentModification extends AbstractModification {

		protected IControlData listData;
		protected Object[] listRawValue;

		public ChangeListContentModification(IControlData listData, Object[] listRawValue, IInfo target) {
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