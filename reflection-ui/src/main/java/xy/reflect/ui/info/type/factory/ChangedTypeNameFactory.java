package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Type information factory that renames a type.
 * 
 * @author olitank
 *
 */
public class ChangedTypeNameFactory extends InfoProxyFactory {

	protected String sourceTypeName;
	protected String targetTypeName;

	public ChangedTypeNameFactory(String sourceTypeName, String targetTypeName) {
		this.sourceTypeName = sourceTypeName;
		this.targetTypeName = targetTypeName;
	}

	public String getSourceTypeName() {
		return sourceTypeName;
	}

	public void setSourceTypeName(String sourceTypeName) {
		this.sourceTypeName = sourceTypeName;
	}

	public String getTargetTypeName() {
		return targetTypeName;
	}

	public void setTargetTypeName(String targetTypeName) {
		this.targetTypeName = targetTypeName;
	}

	@Override
	public String getIdentifier() {
		return "ChangedTypeNameFactory [sourceTypeName=" + sourceTypeName + ", targetTypeName=" + targetTypeName + "]";
	}

	@Override
	protected String getName(ITypeInfo type) {
		if (type.getName().equals(sourceTypeName)) {
			return targetTypeName;
		}
		return type.getName();
	}

}