package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition, IInfo modificationTarget) {
		super(anyListItemPosition, modificationTarget);
	}

	@Override
	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
		return new ListModification(itemPosition, newListRawValue, target) {

			@Override
			protected Runnable createDoJob() {
				final Runnable baseResult = super.createDoJob();
				if (baseResult == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						baseResult.run();
						((BufferedItemPosition) itemPosition).refreshBranch();
					}
				};
			}

			@Override
			protected Runnable createUndoJob() {
				final Runnable baseResult = super.createUndoJob();
				if (baseResult == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						baseResult.run();
						((BufferedItemPosition) itemPosition).refreshBranch();
					}
				};
			}

		};
	}

}
