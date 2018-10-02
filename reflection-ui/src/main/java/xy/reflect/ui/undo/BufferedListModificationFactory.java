package xy.reflect.ui.undo;

import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition, Object rootListValue,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		super(anyListItemPosition, rootListValue, rootListValueCommitModificationAccessor);
	}

	@Override
	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		return new BufferedListModification(itemPosition, newListRawValue, rootListValue,
				rootListValueCommitModificationAccessor);
	}

	public static class BufferedListModification extends ListModification {

		public BufferedListModification(ItemPosition itemPosition, Object[] newListRawValue, Object rootListValue,
				Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			super(itemPosition, newListRawValue, rootListValue, rootListValueCommitModificationAccessor);
		}

		@Override
		public IModification applyAndGetOpposite() {
			ListModification opposite = (ListModification) super.applyAndGetOpposite();
			((BufferedItemPosition) itemPosition).refreshBranch();
			return new BufferedListModification(opposite.itemPosition, opposite.newListRawValue, opposite.rootListValue,
					opposite.rootListValueCommitModificationAccessor);
		}

	}

}
