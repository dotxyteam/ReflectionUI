package xy.reflect.ui.undo;

import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.Mapper;

public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition,
			Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
		super(anyListItemPosition, rootListValueCommitModificationAccessor);
	}

	@Override
	public IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue) {
		return new BufferedListModification(itemPosition, newListRawValue, rootListValueCommitModificationAccessor);
	}

	public static class BufferedListModification extends ListModification {

		public BufferedListModification(ItemPosition itemPosition, Object[] newListRawValue,
				Mapper<Object, IModification> rootListValueCommitModificationAccessor) {
			super(itemPosition, newListRawValue, rootListValueCommitModificationAccessor);
		}

		@Override
		public IModification applyAndGetOpposite() {
			ListModification opposite = (ListModification) super.applyAndGetOpposite();
			((BufferedItemPosition) itemPosition).refreshBranch();
			return new BufferedListModification(opposite.itemPosition, opposite.newListRawValue,
					opposite.rootListValueCommitModificationAccessor);
		}

	}

}
