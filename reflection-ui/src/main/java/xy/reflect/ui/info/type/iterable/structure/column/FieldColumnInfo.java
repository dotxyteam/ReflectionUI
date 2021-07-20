


package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Column that displays the value of the specified item field.
 * 
 * @author olitank
 *
 */
public class FieldColumnInfo extends AbstractInfo implements IColumnInfo {

	protected IFieldInfo field;
	protected ITypeInfo ownerType;
	protected ReflectionUI reflectionUI;

	public FieldColumnInfo(ReflectionUI reflectionUI, ITypeInfo ownerType, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.ownerType = ownerType;
		this.field = field;
	}

	@Override
	public String getCellValue(ItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		Object value = field.getValue(item);
		return ReflectionUIUtils.toString(reflectionUI, value);
	}

	@Override
	public boolean hasCellValue(ItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item == null) {
			return false;
		}
		return ownerType.supports(item);
	}

	@Override
	public int getMinimalCharacterCount() {
		return 20;
	}

	@Override
	public String getCaption() {
		return field.getCaption();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getOnlineHelp() {
		return field.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
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
		FieldColumnInfo other = (FieldColumnInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldColumnInfo [field=" + field + ", ownerType=" + ownerType + "]";
	}

}
