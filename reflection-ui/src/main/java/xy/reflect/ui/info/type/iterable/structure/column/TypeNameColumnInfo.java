


package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntry;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Column that displays the type of the current item.
 * 
 * Note that for map entries the key string representation is displayed.
 * 
 * @author olitank
 *
 */
public class TypeNameColumnInfo extends AbstractInfo implements IColumnInfo {

	protected ReflectionUI reflectionUI;

	public TypeNameColumnInfo(ReflectionUI reflectionUI) {
		super();
		this.reflectionUI = reflectionUI;
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
		if (item instanceof StandardMapEntry) {
			return ReflectionUIUtils.toString(reflectionUI, ((StandardMapEntry) item).getKey());
		} else {
			return reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(item)).getCaption();
		}
	}

	@Override
	public int getMinimalCharacterCount() {
		return 20;
	}

	@Override
	public String getCaption() {
		return "Type";
	}

	@Override
	public String getName() {
		return "typeName";
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
		TypeNameColumnInfo other = (TypeNameColumnInfo) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeNameColumnInfo []";
	}

}
