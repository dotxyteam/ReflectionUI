


package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Column that displays the position (1 + index) of the current item in the
 * list.
 * 
 * @author olitank
 *
 */
public class PositionColumnInfo extends AbstractInfo implements IColumnInfo {

	protected ReflectionUI reflectionUI;

	public PositionColumnInfo(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public String getCellValue(ItemPosition itemPosition) {
		if (itemPosition.isRoot()) {
			return ReflectionUIUtils.toString(reflectionUI, new Integer(itemPosition.getIndex() + 1));
		} else {
			return null;
		}
	}

	@Override
	public boolean hasCellValue(ItemPosition itemPosition) {
		return itemPosition.isRoot();
	}

	@Override
	public int getMinimalCharacterCount() {
		return 5;
	}

	@Override
	public String getCaption() {
		return "N°";
	}

	@Override
	public String getName() {
		return "position";
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PositionColumnInfo []";
	}

}
