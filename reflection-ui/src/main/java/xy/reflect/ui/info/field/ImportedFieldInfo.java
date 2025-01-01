
package xy.reflect.ui.info.field;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field proxy allowing to import a source field in any type. Note that the
 * source field must be static.
 * 
 * @author olitank
 *
 */
public class ImportedFieldInfo extends FieldInfoProxy {

	protected String targetFieldName;

	public ImportedFieldInfo(ITypeInfo sourceType, String sourceFieldName, String targetFieldName) {
		super(retrieveSourceField(sourceType, sourceFieldName));
		this.targetFieldName = targetFieldName;
	}

	protected static IFieldInfo retrieveSourceField(ITypeInfo sourceType, String sourceFieldName) {
		IFieldInfo result = ReflectionUIUtils.findInfoByName(sourceType.getFields(), sourceFieldName);
		if (result == null) {
			throw new ReflectionUIError("'" + sourceFieldName + "' not found in type '" + sourceType + "'");
		}
		return result;
	}

	@Override
	public String getName() {
		if (targetFieldName != null) {
			return targetFieldName;
		} else {
			return super.getName();
		}
	}

	@Override
	public String getCaption() {
		if (targetFieldName != null) {
			return ReflectionUIUtils.identifierToCaption(targetFieldName);
		} else {
			return super.getCaption();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((targetFieldName == null) ? 0 : targetFieldName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportedFieldInfo other = (ImportedFieldInfo) obj;
		if (targetFieldName == null) {
			if (other.targetFieldName != null)
				return false;
		} else if (!targetFieldName.equals(other.targetFieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImportedFieldInfo [sourceField=" + base + ", targetFieldName=" + targetFieldName + "]";
	}

}
