


package xy.reflect.ui.undo;

import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;

/**
 * This class is a sub-class of {@link ListModificationFactory} that works with
 * {@link BufferedItemPosition} instances.
 * 
 * @author olitank
 *
 */
public class BufferedListModificationFactory extends ListModificationFactory {

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition) {
		super(anyListItemPosition);
	}

	@Override
	public IModification createListModification(Object[] newListRawValue) {
		return new BufferedListModification((BufferedItemPosition) anyItemPosition, newListRawValue,
				anyItemPosition.retrieveContainingListRawValue());
	}

	/**
	 * This class is a sub-class of {@link ListModification} that works with
	 * {@link BufferedItemPosition} instances.
	 * 
	 * Note that the buffered items should be refreshed after the execution of a
	 * modification of this type.
	 * 
	 * @author olitank
	 *
	 */
	public static class BufferedListModification extends ListModification {

		public BufferedListModification(BufferedItemPosition itemPosition, Object[] newListRawValue,
				Object[] oldListRawValue) {
			super(itemPosition, newListRawValue, oldListRawValue);
		}

		@Override
		public IModification applyAndGetOpposite() {
			ListModification opposite = (ListModification) super.applyAndGetOpposite();
			return new BufferedListModification((BufferedItemPosition) opposite.itemPosition, opposite.newListRawValue,
					opposite.oldListRawValue);
		}

	}

}
