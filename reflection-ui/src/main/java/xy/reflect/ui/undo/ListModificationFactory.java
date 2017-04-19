package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class ListModificationFactory {

	protected ItemPosition anyItemPosition;
	protected IInfo modificationTarget;

	public ListModificationFactory(ItemPosition anyListItemPosition, IInfo modificationTarget) {
		this.anyItemPosition = anyListItemPosition;
		this.modificationTarget = modificationTarget;
	}

	public boolean canAdd(int index) {
		if ((index < 0) || (index > anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListValueUpdateModification.isCompatibleWith(anyItemPosition);
	}

	public IModification add(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return new ListValueUpdateModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canRemove(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListValueUpdateModification.isCompatibleWith(anyItemPosition);
	}

	public IModification remove(int index) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.remove(index);
		Object[] newListRawValue = tmpList.toArray();
		return new ListValueUpdateModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canSet(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListValueUpdateModification.isCompatibleWith(anyItemPosition);
	}

	public IModification set(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.set(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return new ListValueUpdateModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canMove(int index, int offset) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		int index2 = index + offset;
		if ((index2 < 0) || (index2 >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListValueUpdateModification.isCompatibleWith(anyItemPosition);
	}

	public IModification move(int index, int offset) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index + offset, tmpList.remove(index));
		Object[] newListRawValue = tmpList.toArray();
		return new ListValueUpdateModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canClear() {
		return ListValueUpdateModification.isCompatibleWith(anyItemPosition);
	}

	public IModification clear() {
		return new ListValueUpdateModification(anyItemPosition, new Object[0], modificationTarget);
	}

	protected static class ListValueUpdateModification extends AbstractModification {

		protected ItemPosition itemPosition;
		protected Object[] newListRawValue;

		protected List<IModification> undoModifications;

		public ListValueUpdateModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
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
			if (containingListType.canReplaceContent()) {
				if (itemPosition.geContainingListReturnMode() == ValueReturnMode.DIRECT_OR_PROXY) {
					return true;
				}
				if (!itemPosition.isContainingListGetOnly()) {
					return true;
				}
			}
			if (containingListType.canInstanciateFromArray()) {
				if (!itemPosition.isContainingListGetOnly()) {
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
					new CompositeModification(target, null, UndoOrder.getNormal(), undoModifications)
							.applyAndGetOpposite();
					undoModifications = null;
				}

			};
		}

		protected void updateListValueRecursively(ItemPosition itemPosition, Object[] listRawValue) {
			if (!isCompatibleWith(itemPosition)) {
				return;
			}

			if (itemPosition.isRoot()) {
				IFieldControlData listData = itemPosition.getRootListData();
				updateListValue(listData, listRawValue);
			} else {
				ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
				Object parentItem = parentItemPosition.getItem();

				updateListValue(new DefaultFieldControlData(parentItem, itemPosition.getContainingListFieldIfNotRoot()),
						listRawValue);

				Object[] parentListRawValue = parentItemPosition.retrieveContainingListRawValue();
				if (parentItem != parentListRawValue[parentItemPosition.getIndex()]) {
					parentListRawValue[parentItemPosition.getIndex()] = parentItem;
				}
				updateListValueRecursively(parentItemPosition, parentListRawValue);
			}
		}

		protected void updateListValue(IFieldControlData listData, Object[] newListRawValue) {
			IListTypeInfo listType = (IListTypeInfo) listData.getType();
			if (listType.canReplaceContent()) {
				if (listData.getValueReturnMode() == ValueReturnMode.DIRECT_OR_PROXY) {
					undoModifications.add(0,
							new ChangeListContentModification(listData, newListRawValue, target).applyAndGetOpposite());
					return;
				}
				if (!listData.isGetOnly()) {
					final Object listValue = listData.getValue();
					undoModifications.add(0, new ChangeListContentModification(new FieldControlDataProxy(listData) {
						@Override
						public Object getValue() {
							return listValue;
						}
					}, newListRawValue, target).applyAndGetOpposite());
					undoModifications.add(0,
							new ControlDataValueModification(listData, listValue, target).applyAndGetOpposite());
					return;
				}
			}
			if (listType.canInstanciateFromArray()) {
				if (!listData.isGetOnly()) {
					Object listValue = listType.fromArray(newListRawValue);
					undoModifications.add(0,
							new ControlDataValueModification(listData, listValue, target).applyAndGetOpposite());
					return;
				}
			}
			throw new ReflectionUIError();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemPosition == null) ? 0 : itemPosition.hashCode());
			result = prime * result + Arrays.hashCode(newListRawValue);
			result = prime * result + ((undoModifications == null) ? 0 : undoModifications.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ListValueUpdateModification other = (ListValueUpdateModification) obj;
			if (itemPosition == null) {
				if (other.itemPosition != null)
					return false;
			} else if (!itemPosition.equals(other.itemPosition))
				return false;
			if (!Arrays.equals(newListRawValue, other.newListRawValue))
				return false;
			if (undoModifications == null) {
				if (other.undoModifications != null)
					return false;
			} else if (!undoModifications.equals(other.undoModifications))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListValueUpdateModification [itemPosition=" + itemPosition + ", newListRawValue="
					+ Arrays.toString(newListRawValue) + ", undoModifications=" + undoModifications + "]";
		}

	}

	protected static class ChangeListContentModification extends AbstractModification {

		protected IFieldControlData listData;
		protected Object[] listRawValue;

		public ChangeListContentModification(IFieldControlData listData, Object[] listRawValue, IInfo target) {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((listData == null) ? 0 : listData.hashCode());
			result = prime * result + Arrays.hashCode(listRawValue);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChangeListContentModification other = (ChangeListContentModification) obj;
			if (listData == null) {
				if (other.listData != null)
					return false;
			} else if (!listData.equals(other.listData))
				return false;
			if (!Arrays.equals(listRawValue, other.listRawValue))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ChangeListContentModification [listData=" + listData + ", listRawValue="
					+ Arrays.toString(listRawValue) + "]";
		}

	}

}
