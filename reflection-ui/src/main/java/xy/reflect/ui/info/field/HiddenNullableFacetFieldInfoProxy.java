package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.factory.HiddenNullableFacetsTypeInfoProxyFactory;

public class HiddenNullableFacetFieldInfoProxy extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo base;

	public HiddenNullableFacetFieldInfoProxy(final ReflectionUI reflectionUI, final IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@SuppressWarnings("unused")
	@Override
	public Object getValue(final Object object) {
		final Object[] result = new Object[1];
		new HiddenNullableFacetsTypeInfoProxyFactory(reflectionUI) {
			{
				result[0] = getValue(object, base, null);
			}
		};
		return result[0];
	}

	@Override
	public boolean isValueNullable() {
		return false;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		HiddenNullableFacetFieldInfoProxy other = (HiddenNullableFacetFieldInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HiddenNullableFacetFieldInfoProxy [base=" + base + "]";
	}

}
