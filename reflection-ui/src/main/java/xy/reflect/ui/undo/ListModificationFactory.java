package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class ListModificationFactory {

	protected ItemPosition anyItemPosition;
	protected IInfo modificationTarget;
	protected Object rootListValue;
	protected Mapper<Object, IModification> rootListValueCommitModificationAccessor;

	public ListModificationFactory(ItemPosition anyListItemPosition, Object rootListValue, IInfo modificationTarget,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		this.anyItemPosition = anyListItemPosition;
		this.modificationTarget = modificationTarget;
		this.rootListValue = rootListValue;
		this.rootListValueCommitModificationAccessor = rootListValueCommitModificationAccessor;
	}

	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		return new ListModification(itemPosition, newListRawValue, rootListValue, target,
				rootListValueCommitModificationAccessor);
	}

	public boolean canAdd(int index) {
		if ((index < 0) || (index > anyItemPosition.getContainingListSize(rootListValue))) {
			return false;
		}
		return anyItemPosition.isContainingListEditable(rootListValue);
	}

	public boolean canAddAll(int index, List<Object> items) {
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
		List<Object> tmpList = new ArrayList<Object>(
				Arrays.asList(anyItemPosition.retrieveContainingListRawValue(rootListValue)));
		tmpList.add(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget,
				rootListValueCommitModificationAccessor);
	}

	public boolean canRemove(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize(rootListValue))) {
			return false;
		}
		return anyItemPosition.isContainingListEditable(rootListValue);
	}

	public IModification remove(int index) {
		List<Object> tmpList = new ArrayList<Object>(
				Arrays.asList(anyItemPosition.retrieveContainingListRawValue(rootListValue)));
		tmpList.remove(index);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget,
				rootListValueCommitModificationAccessor);
	}

	public boolean canSet(int index) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize(rootListValue))) {
			return false;
		}
		return anyItemPosition.isContainingListEditable(rootListValue);
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
		List<Object> tmpList = new ArrayList<Object>(
				Arrays.asList(anyItemPosition.retrieveContainingListRawValue(rootListValue)));
		tmpList.set(index, newItem);
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget,
				rootListValueCommitModificationAccessor);
	}

	public boolean canMove(int index, int offset) {
		if ((index < 0) || (index >= anyItemPosition.getContainingListSize(rootListValue))) {
			return false;
		}
		int index2 = index + offset;
		if ((index2 < 0) || (index2 >= anyItemPosition.getContainingListSize(rootListValue))) {
			return false;
		}
		return anyItemPosition.isContainingListEditable(rootListValue);
	}

	public IModification move(int index, int offset) {
		List<Object> tmpList = new ArrayList<Object>(
				Arrays.asList(anyItemPosition.retrieveContainingListRawValue(rootListValue)));
		tmpList.add(index + offset, tmpList.remove(index));
		Object[] newListRawValue = tmpList.toArray();
		return createListModification(anyItemPosition, newListRawValue, modificationTarget,
				rootListValueCommitModificationAccessor);
	}

	public boolean canClear() {
		return anyItemPosition.isContainingListEditable(rootListValue);
	}

	public IModification clear() {
		return createListModification(anyItemPosition, new Object[0], modificationTarget,
				rootListValueCommitModificationAccessor);
	}

	protected static class ListModification implements IModification {

		protected ItemPosition itemPosition;
		protected Object[] newListRawValue;
		protected Object rootListValue;
		protected Mapper<Object, IModification> rootListValueCommitModificationAccessor;
		protected IInfo target;

		public ListModification(ItemPosition itemPosition, Object[] newListRawValue, Object rootListValue, IInfo target,
				Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			this.target = target;
			this.itemPosition = itemPosition;
			this.newListRawValue = newListRawValue;
			this.rootListValue = rootListValue;
			this.rootListValueCommitModificationAccessor = rootListValueCommitModificationAccessor;
		}

		@Override
		public String getTitle() {
			return ControlDataValueModification.getTitle(target);
		}

		@Override
		public IModification applyAndGetOpposite() {
			Object[] oldListRawValue = itemPosition.retrieveContainingListRawValue(rootListValue);
			Object newRootListValue = itemPosition.updateContainingList(newListRawValue, rootListValue);
			if (newRootListValue == null) {
				newRootListValue = rootListValue;
			}
			rootListValueCommitModificationAccessor.get(newRootListValue).applyAndGetOpposite();
			return new ListModification(itemPosition, oldListRawValue, newRootListValue, target,
					rootListValueCommitModificationAccessor);
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public IInfo getTarget() {
			return target;
		}

	}

}
