


package xy.reflect.ui.info.type.source;

/**
 * This class allows to include a context (typically the reference to the owner
 * field) in an abstract UI type information so that the appearance and the
 * behavior of the generated control can be automatically adjusted in specific
 * contexts (in addition to the global adjustments).
 * 
 * @author olitank
 *
 */
public class SpecificitiesIdentifier {

	protected String objectTypeName;
	protected String fieldName;

	public SpecificitiesIdentifier(String objectTypeName, String fieldName) {
		super();
		this.objectTypeName = objectTypeName;
		this.fieldName = fieldName;
	}

	public String getobjectTypeName() {
		return objectTypeName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectTypeName == null) ? 0 : objectTypeName.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		SpecificitiesIdentifier other = (SpecificitiesIdentifier) obj;
		if (objectTypeName == null) {
			if (other.objectTypeName != null)
				return false;
		} else if (!objectTypeName.equals(other.objectTypeName))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SpecificitiesIdentifier [objectTypeName=" + objectTypeName + ", fieldName=" + fieldName + "]";
	}

}
