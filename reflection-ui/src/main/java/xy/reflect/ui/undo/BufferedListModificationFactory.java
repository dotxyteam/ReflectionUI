package xy.reflect.ui.undo;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public class BufferedListModificationFactory extends ListModificationFactory{

	public BufferedListModificationFactory(BufferedItemPosition anyListItemPosition, IInfo modificationTarget) {
		super(anyListItemPosition, modificationTarget);
	}

	@Override
	protected IModification createListModification(ItemPosition itemPosition, Object[] newListRawValue, IInfo target) {
		return new ListModification(itemPosition, newListRawValue, target){

			@Override
			protected void updateListValue(IFieldControlData listData, Object[] newListRawValue) {
				super.updateListValue(listData, newListRawValue);
				((BufferedItemPosition)itemPosition).refreshBranch();
			}
			
		};
	}
	
	

}
