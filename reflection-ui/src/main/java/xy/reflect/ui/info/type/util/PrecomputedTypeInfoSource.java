package xy.reflect.ui.info.type.util;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((precomputedType == null) ? 0 : precomputedType.hashCode());
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
		PrecomputedTypeInfoSource other = (PrecomputedTypeInfoSource) obj;
		if (precomputedType == null) {
			if (other.precomputedType != null)
				return false;
		} else if (!precomputedType.equals(other.precomputedType))
			return false;
		return true;
	}
	
	

}
