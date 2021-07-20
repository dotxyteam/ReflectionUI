


package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Column that displays the string representation of the current item.
 * 
 * @author olitank
 *
 */
public class StringValueColumnInfo extends AbstractInfo implements IColumnInfo {

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
		Object item = itemPosition.getItem();
		if (item == null) {
			return "";
		}
		return ReflectionUIUtils.toString(reflectionUI, item);
	}

	@Override
	public int getMinimalCharacterCount() {
		return 30;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StringValueColumnInfo []";
	}

}
