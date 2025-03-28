
package xy.reflect.ui.control;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Context that builds its identifier from the given field and its containing
 * type.
 * 
 * @author olitank
 *
 */
public class FieldContext implements IContext {

	protected ITypeInfo objectType;
	protected IFieldInfo field;

	public FieldContext(ITypeInfo objectType, IFieldInfo field) {
		this.objectType = objectType;
		this.field = field;
	}

	@Override
	public String getIdentifier() {
		return "FieldContext [fieldName=" + field.getName() + ", objectType=" + objectType.getName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
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
		FieldContext other = (FieldContext) obj;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldContext [field=" + field + ", objectType=" + objectType + "]";
	}

}
