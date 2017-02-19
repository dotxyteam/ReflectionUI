package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.type.iterable.util.ItemPosition;

public class PositionColumnInfo implements IColumnInfo {

	@Override
	public String getCellValue(ItemPosition itemPosition) {
		if (itemPosition.isRootListItemPosition()) {
			return Integer.toString(itemPosition.getIndex() + 1);
		} else {
			return null;
		}
	}

	@Override
	public boolean hasCellValue(ItemPosition itemPosition) {
		return itemPosition.isRootListItemPosition();
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
	public String toString() {
		return "PositionColumnInfo []";
	}

}
