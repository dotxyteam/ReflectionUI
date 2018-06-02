package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition, Object rootListValue,
			IInfo modificationTarget, Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		super(anyListItemPosition, rootListValue, modificationTarget, rootListValueCommitModificationAccessor);
	}

	@Override
	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		return new BufferedListModification(itemPosition, newListRawValue, rootListValue, target,
				rootListValueCommitModificationAccessor);
	}

	public static class BufferedListModification extends ListModification {

		public BufferedListModification(ItemPosition itemPosition, Object[] newListRawValue, Object rootListValue,
				IInfo target, Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			super(itemPosition, newListRawValue, rootListValue, target, rootListValueCommitModificationAccessor);
		}

		@Override
		public IModification applyAndGetOpposite() {
			ListModification opposite = (ListModification) super.applyAndGetOpposite();
			((BufferedItemPosition) itemPosition).refreshBranch();
			return new BufferedListModification(opposite.itemPosition, opposite.newListRawValue, opposite.rootListValue,
					opposite.target, opposite.rootListValueCommitModificationAccessor);
		}

	}

}
