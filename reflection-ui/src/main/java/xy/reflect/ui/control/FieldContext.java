
package xy.reflect.ui.control;

/**
 * Context that builds its identifier from the given field and its containing
 * type.
 * 
 * @author olitank
 *
 */
public class FieldContext implements IContext {

	public static final FieldContext NULL_FIELD_CONTEXT = new FieldContext(null, null) {
		@Override
		public String getIdentifier() {
			return "NULL_FIELD_CONTEXT";
		}
	};

	protected String objectTypeName;
	protected String fieldName;

	public FieldContext(String objectTypeName, String fieldName) {
		this.objectTypeName = objectTypeName;
		this.fieldName = fieldName;
	}

	@Override
	public String getIdentifier() {
		return "FieldContext [fieldName=" + fieldName + ", objectType=" + objectTypeName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((objectTypeName == null) ? 0 : objectTypeName.hashCode());
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
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (objectTypeName == null) {
			if (other.objectTypeName != null)
				return false;
		} else if (!objectTypeName.equals(other.objectTypeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

}
