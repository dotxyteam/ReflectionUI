package xy.reflect.ui.info.type;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class DefaultListStructuralInfo extends
		AbstractTreeDetectionListStructuralInfo {


	public DefaultListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		super(reflectionUI, rootItemType);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		return "";
	}

	@Override
	public String getCellValue(IItemPosition itemPosition, int columnIndex) {
		if (columnIndex != 0) {
			throw new ReflectionUIError();
		}
		return reflectionUI.toString(itemPosition.getItem());
	}

	@Override
	protected boolean isFieldBased() {
		return false;
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return true;
	}
	
	

}
