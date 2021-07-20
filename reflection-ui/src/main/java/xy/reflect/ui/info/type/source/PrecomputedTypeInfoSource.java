


package xy.reflect.ui.info.type.source;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;

/**
 * Dynamic type information source that just provides the specified
 * {@link ITypeInfo} that should have been precomputed elsewhere.
 * 
 * @author olitank
 *
 */
public class PrecomputedTypeInfoSource implements ITypeInfoSource {

	protected ITypeInfo precomputedType;
	protected SpecificitiesIdentifier specificitiesIdentifier;

	public PrecomputedTypeInfoSource(ITypeInfo precomputedType, SpecificitiesIdentifier specificitiesIdentifier) {
		this.precomputedType = precomputedType;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	@Override
	public ITypeInfo getTypeInfo() {
		return new InfoProxyFactory() {

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return PrecomputedTypeInfoSource.this;
			}

			@Override
			public String getIdentifier() {
				return PrecomputedTypeInfoSource.this.toString();
			}

		}.wrapTypeInfo(precomputedType);
	}

	@Override
	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return specificitiesIdentifier;
	}

	public ITypeInfo getPrecomputedType() {
		return precomputedType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((precomputedType == null) ? 0 : precomputedType.hashCode());
		result = prime * result + ((specificitiesIdentifier == null) ? 0 : specificitiesIdentifier.hashCode());
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
		if (specificitiesIdentifier == null) {
			if (other.specificitiesIdentifier != null)
				return false;
		} else if (!specificitiesIdentifier.equals(other.specificitiesIdentifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PrecomputedTypeInfoSource [precomputedType=" + precomputedType + ", specificitiesIdentifier="
				+ specificitiesIdentifier + "]";
	}

}
