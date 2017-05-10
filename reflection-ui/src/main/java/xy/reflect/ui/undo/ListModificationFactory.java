package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
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

	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
		return new ListModification(itemPosition, newListRawValue, target);
	}

	public boolean canAdd(int index) {
		if ((index < 0) || (index > anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListModification.isCompatibleWith(anyItemPosition);
	}

	public boolean canAdd(int index, List<Object> items) {
		if (!canAdd(index)) {
			return false;
		}
		ITypeInfo itemType = anyItemPosition.getContainingListType().getItemType();
		if (itemType != null) {
			for (Object item : items) {
				if (!itemType.supportsInstance(item)) {
					return false;
				}
			}
		}
		return true;
	}

	public IModification add(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canRemove(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListModification.isCompatibleWith(anyItemPosition);
	}

	public IModification remove(int index) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.remove(index);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canSet(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListModification.isCompatibleWith(anyItemPosition);
	}

	public boolean canSet(int index, Object item) {
		if (!canSet(index)) {
			return false;
		}
		ITypeInfo itemType = anyItemPosition.getContainingListType().getItemType();
		if (itemType != null) {
			if (!itemType.supportsInstance(item)) {
				return false;
			}
		}
		return true;
	}

	public IModification set(int index, Object newItem) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.set(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canMove(int index, int offset) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		int index2 = index + offset;
		if ((index2 < 0) || (index2 >= anyItemPosition.getContainingListSize())) {
			return false;
		}
		return ListModification.isCompatibleWith(anyItemPosition);
	}

	public IModification move(int index, int offset) {
		List<Object> tmpList = new ArrayList<Object>(Arrays.asList(anyItemPosition.retrieveContainingListRawValue()));
		tmpList.add(index + offset, tmpList.remove(index));
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget);
	}

	public boolean canClear() {
		return ListModification.isCompatibleWith(anyItemPosition);
	}

	public IModification clear() {
		return createListModification(anyItemPosition, new Object[0], modificationTarget);
	}

	protected static class ListModification extends AbstractModification {

		protected ItemPosition itemPosition;
		protected Object[] newListRawValue;

		protected List<IModification> subUndoModifications;

		public ListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
			super(target);
			this.itemPosition = itemPosition;
			this.newListRawValue = newListRawValue;
		}

		public static boolean isCompatibleWith(ItemPosition itemPosition) {
			Object[] exampleNewListRawValue = new Object[0];
			IInfo exampleTarget = IFieldInfo.NULL_FIELD_INFO;
			ListModification exampleListModification = new ListModification(itemPosition, exampleNewListRawValue,
					exampleTarget);
			List<IModification> exampleSubModifications = exampleListModification
					.listSubModificationsRecursively(itemPosition, exampleNewListRawValue);
			return exampleSubModifications != null;
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
					List<IModification> subModifactions = listSubModificationsRecursively(itemPosition,
							newListRawValue);
					subUndoModifications = new ArrayList<IModification>();
					for (IModification modif : subModifactions) {
						subUndoModifications.add(0, modif.applyAndGetOpposite());
					}
				}

			};
		}

		@Override
		protected Runnable createUndoJob() {
			return new Runnable() {
				@Override
				public void run() {
					for (IModification modif : subUndoModifications) {
						modif.applyAndGetOpposite();
					}
					subUndoModifications = null;
				}

			};
		}

		protected static IFieldControlData getContainingListData(ItemPosition itemPosition) {
			if (itemPosition.isRoot()) {
				return itemPosition.getRootListData();
			} else {
				ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
				Object parentItem = parentItemPosition.getItem();
				return new DefaultFieldControlData(parentItem, itemPosition.getContainingListFieldIfNotRoot());
			}
		}

		protected void checkListRawValue(IListTypeInfo listType, Object[] listRawValue) {
			ITypeInfo itemType = listType.getItemType();
			if (itemType != null) {
				for (Object item : listRawValue) {
					if (item != null) {
						if (!itemType.supportsInstance(item)) {
							throw new ReflectionUIError("Item not supported: '" + item
									+ "'. Was expecting instance of '" + itemType.getName() + "'");

						}
					}
				}
			}
		}

		protected List<IModification> listSubModificationsRecursively(ItemPosition itemPosition,
				Object[] listRawValue) {
			IFieldControlData listData = getContainingListData(itemPosition);
			List<IModification> result = listSubModifications(listData, listRawValue);
			if (result == null) {
				return null;
			}
			if (!itemPosition.isRoot()) {
				Object updatedParentItem = ((DefaultFieldControlData) listData).getObject();
				ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
				Object[] parentListRawValue = parentItemPosition.retrieveContainingListRawValue();
				if (updatedParentItem != parentListRawValue[parentItemPosition.getIndex()]) {
					parentListRawValue[parentItemPosition.getIndex()] = updatedParentItem;
				}

				List<IModification> parentResult = listSubModificationsRecursively(parentItemPosition,
						parentListRawValue);
				if (parentResult == null) {
					return null;
				}
				result = new ArrayList<IModification>(result);
				result.addAll(parentResult);
			}
			return result;
		}

		protected List<IModification> listSubModifications(IFieldControlData listData, Object[] newListRawValue) {
			IListTypeInfo listType = (IListTypeInfo) listData.getType();
			checkListRawValue(listType, newListRawValue);
			if (listType.canReplaceContent()) {
				if (listData.getValue() != null) {
					if (listData.getValueReturnMode() == ValueReturnMode.DIRECT_OR_PROXY) {
						return Collections.<IModification>singletonList(
								new ChangeListContentModification(listData, newListRawValue, target));
					}
					if (!listData.isGetOnly()) {
						final Object listValue = listData.getValue();
						List<IModification> result = new ArrayList<IModification>();
						result.add(new ChangeListContentModification(new FieldControlDataProxy(listData) {
							@Override
							public Object getValue() {
								return listValue;
							}
						}, newListRawValue, target));
						result.add(new ControlDataValueModification(listData, listValue, target));
						return result;
					}
				}
			}
			if (listType.canInstanciateFromArray()) {
				if (!listData.isGetOnly()) {
					return Collections.<IModification>singletonList(
							new ReplaceListValueModification(listData, newListRawValue, target));
				}
			}
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemPosition == null) ? 0 : itemPosition.hashCode());
			result = prime * result + Arrays.hashCode(newListRawValue);
			result = prime * result + ((subUndoModifications == null) ? 0 : subUndoModifications.hashCode());
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
			ListModification other = (ListModification) obj;
			if (itemPosition == null) {
				if (other.itemPosition != null)
					return false;
			} else if (!itemPosition.equals(other.itemPosition))
				return false;
			if (!Arrays.equals(newListRawValue, other.newListRawValue))
				return false;
			if (subUndoModifications == null) {
				if (other.subUndoModifications != null)
					return false;
			} else if (!subUndoModifications.equals(other.subUndoModifications))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListValueUpdateModification [itemPosition=" + itemPosition + ", newListRawValue="
					+ Arrays.toString(newListRawValue) + ", undoModifications=" + subUndoModifications + "]";
		}

	}

	protected static class ReplaceListValueModification extends AbstractModification {

		protected IFieldControlData listData;
		protected Object[] listRawValue;

		protected IModification delegate;
		protected IModification delegateOpposite;

		public ReplaceListValueModification(IFieldControlData listData, Object[] listRawValue, IInfo target) {
			super(target);
			this.listData = listData;
			this.listRawValue = listRawValue;

			IListTypeInfo listType = (IListTypeInfo) listData.getType();
			Object listValue = listType.fromArray(listRawValue);
			delegate = new ControlDataValueModification(listData, listValue, target);

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
					delegateOpposite = delegate.applyAndGetOpposite();
				}
			};
		}

		@Override
		protected Runnable createUndoJob() {
			return new Runnable() {

				@Override
				public void run() {
					delegateOpposite.applyAndGetOpposite();
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
			ReplaceListValueModification other = (ReplaceListValueModification) obj;
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
			return "ReplaceListValueModification [listData=" + listData + ", listRawValue="
					+ Arrays.toString(listRawValue) + "]";
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
