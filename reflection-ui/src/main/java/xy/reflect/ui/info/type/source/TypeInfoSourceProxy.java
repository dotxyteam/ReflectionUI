
package xy.reflect.ui.info.type.source;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;

/**
 * Dynamic type information source proxy class. The methods in this class should
 * be overriden to provide a custom behavior.
 * 
 * @author olitank
 *
 */
public abstract class TypeInfoSourceProxy implements ITypeInfoSource {

	protected ITypeInfoSource base;

	protected abstract String getTypeInfoProxyFactoryIdentifier();

	public TypeInfoSourceProxy(ITypeInfoSource base) {
		super();
		this.base = base;
	}

	@Override
	public ITypeInfo getTypeInfo() {
		return new InfoProxyFactory() {

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return TypeInfoSourceProxy.this;
			}

			@Override
			public String getIdentifier() {
				return TypeInfoSourceProxy.this.getTypeInfoProxyFactoryIdentifier();
			}

		}.wrapTypeInfo(base.getTypeInfo());
	}

	@Override
	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return base.getSpecificitiesIdentifier();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		TypeInfoSourceProxy other = (TypeInfoSourceProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeInfoSourceProxy [base=" + base + "]";
	}

}
