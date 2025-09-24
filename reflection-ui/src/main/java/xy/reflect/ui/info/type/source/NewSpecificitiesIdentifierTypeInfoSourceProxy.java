package xy.reflect.ui.info.type.source;

public class NewSpecificitiesIdentifierTypeInfoSourceProxy extends TypeInfoSourceProxy {

	protected SpecificitiesIdentifier specificitiesIdentifier;
	protected String typeInfoProxyFactoryIdentifier;

	public NewSpecificitiesIdentifierTypeInfoSourceProxy(ITypeInfoSource base,
			SpecificitiesIdentifier specificitiesIdentifier, String typeInfoProxyFactoryIdentifier) {
		super(base);
		this.specificitiesIdentifier = specificitiesIdentifier;
		this.typeInfoProxyFactoryIdentifier = typeInfoProxyFactoryIdentifier;
	}

	@Override
	protected String getTypeInfoProxyFactoryIdentifier() {
		return typeInfoProxyFactoryIdentifier;
	}

	@Override
	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return specificitiesIdentifier;
	}
}