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

	@Override
	public int hashCode() {
		return precomputedType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PrecomputedTypeInfoSource)){
			return false;
		}
		PrecomputedTypeInfoSource other = (PrecomputedTypeInfoSource) obj;
		if(!precomputedType.equals(other.precomputedType)){
			return false;
		}
		return true;
	}
	
	

}
