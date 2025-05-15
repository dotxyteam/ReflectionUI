package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;

/**
 * Type information factory that renames a type.
 * 
 * @author olitank
 *
 */
public class ChangedTypeNameFactory extends InfoProxyFactory {

	protected ReflectionUI reflectionUI;
	protected String sourceTypeName;
	protected String targetTypeName;

	public ChangedTypeNameFactory(ReflectionUI reflectionUI, String sourceTypeName, String targetTypeName) {
		this.reflectionUI = reflectionUI;
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
	protected ITypeInfo getType(IFieldInfo field, ITypeInfo objectType) {
		return new TypeInfoSourceProxy(super.getType(field, objectType).getSource()) {

			@Override
			public SpecificitiesIdentifier getSpecificitiesIdentifier() {
				return new SpecificitiesIdentifier(targetTypeName, field.getName());
			}

			@Override
			protected String getTypeInfoProxyFactoryIdentifier() {
				return "FieldValueTypeInfoProxyFactory [of=" + ChangedTypeNameFactory.this.getIdentifier() + "]";
			}

		}.buildTypeInfo(reflectionUI);
	}

	@Override
	protected String getName(ITypeInfo type) {
		if (type.getName().equals(sourceTypeName)) {
			return targetTypeName;
		}
		return type.getName();
	}

}