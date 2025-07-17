
package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
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

	protected ReflectionUI reflectionUI;
	protected ITypeInfo sourceObjectType;
	protected String sourceFieldName;
	protected ITypeInfo targetObjectType;
	protected String targetFieldName;

	public ImportedFieldInfo(ReflectionUI reflectionUI, ITypeInfo sourceObjectType, String sourceFieldName,
			ITypeInfo targetObjectType, String targetFieldName) {
		super(retrieveSourceField(sourceObjectType, sourceFieldName));
		this.reflectionUI = reflectionUI;
		this.sourceObjectType = sourceObjectType;
		this.sourceFieldName = sourceFieldName;
		this.targetObjectType = targetObjectType;
		this.targetFieldName = targetFieldName;
	}

	protected static IFieldInfo retrieveSourceField(ITypeInfo sourceObjectType, String sourceFieldName) {
		IFieldInfo result = ReflectionUIUtils.findInfoByName(sourceObjectType.getFields(), sourceFieldName);
		if (result == null) {
			throw new ReflectionUIError("'" + sourceFieldName + "' not found in type '" + sourceObjectType + "'");
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
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(super.getType().getSource()) {

			@Override
			protected String getTypeInfoProxyFactoryIdentifier() {
				return "ImportedFieldInfoProxyFactory [sourceObjectType=" + sourceObjectType.getName()
						+ ", sourceFieldName=" + sourceFieldName + ", targetObjectType=" + targetObjectType.getName()
						+ ", targetFieldName=" + targetFieldName + "]";
			}

			@Override
			public SpecificitiesIdentifier getSpecificitiesIdentifier() {
				return new SpecificitiesIdentifier(targetObjectType.getName(), ImportedFieldInfo.this.getName());
			}
		});
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
