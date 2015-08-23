package xy.reflect.ui.info.type.iterable.util.structure;

import java.awt.Image;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
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
	public String getCellValue(ItemPosition itemPosition, int columnIndex) {
		if (columnIndex != 0) {
			throw new ReflectionUIError();
		}
		return reflectionUI.toString(itemPosition.getItem());
	}

	
	
	@Override
	public Image getCellIconImage(ItemPosition itemPosition, int columnIndex) {
		if (columnIndex != 0) {
			throw new ReflectionUIError();
		}
		return reflectionUI.getIconImage(itemPosition.getItem());
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return true;
	}

	@Override
	public String toString() {
		return "DefaultListStructuralInfo [rootItemType=" + rootItemType + "]";
	}
	
	

}
