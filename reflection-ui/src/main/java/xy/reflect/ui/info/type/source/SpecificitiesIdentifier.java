
package xy.reflect.ui.info.type.source;

import xy.reflect.ui.control.FieldContext;

/**
 * This class allows to include a context (typically the reference to the owner
 * field) in an abstract UI type information so that the appearance and the
 * behavior of the generated control can be automatically adjusted in specific
 * contexts (in addition to the global adjustments).
 * 
 * @author olitank
 *
 */
public class SpecificitiesIdentifier extends FieldContext {

	public SpecificitiesIdentifier(String objectTypeName, String fieldName) {
		super(objectTypeName, fieldName);
	}

	public String getObjectTypeName() {
		return objectTypeName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public String toString() {
		return "SpecificitiesIdentifier [objectTypeName=" + objectTypeName + ", fieldName=" + fieldName + "]";
	}

}
