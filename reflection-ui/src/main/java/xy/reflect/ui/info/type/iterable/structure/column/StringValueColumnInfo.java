package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StringValueColumnInfo implements IColumnInfo {

	protected ReflectionUI reflectionUI;

	public StringValueColumnInfo(ReflectionUI reflectionUI) {
		super();
		this.reflectionUI = reflectionUI;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public String getName() {
		return "toStringValue";
	}

	@Override
	public String getCaption() {
		return "";
	}

	@Override
	public boolean hasCellValue(ItemPosition itemPosition) {
		return true;
	}

	@Override
	public String getCellValue(ItemPosition itemPosition) {
		return ReflectionUIUtils.toString(reflectionUI, itemPosition.getItem());
	}

	@Override
	public int getMinimalCharacterCount() {
		return 30;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringValueColumnInfo other = (StringValueColumnInfo) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StringValueColumnInfo []";
	}

}
