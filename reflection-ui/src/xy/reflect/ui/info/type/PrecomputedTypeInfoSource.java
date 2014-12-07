package xy.reflect.ui.info.type;

public class PrecomputedTypeInfoSource implements ITypeInfoSource {

	protected ITypeInfo precomputedType;

	public PrecomputedTypeInfoSource(ITypeInfo precomputedType) {
		super();
		this.precomputedType = precomputedType;
	}

	public ITypeInfo getPrecomputedType() {
		return precomputedType;
	}

}
