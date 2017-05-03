package xy.reflect.ui.info.parameter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.factory.HiddenNullableFacetsTypeInfoProxyFactory;

public class HiddenNullableFacetParameterInfoProxy extends ParameterInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IParameterInfo base;

	public HiddenNullableFacetParameterInfoProxy(final ReflectionUI reflectionUI, final IParameterInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@SuppressWarnings("unused")
	@Override
	public Object getDefaultValue() {
		final Object[] result = new Object[1];
		new HiddenNullableFacetsTypeInfoProxyFactory(reflectionUI) {
			{
				result[0] = getDefaultValue(base, null, null);
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
		HiddenNullableFacetParameterInfoProxy other = (HiddenNullableFacetParameterInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HiddenNullableFacetParameterInfoProxy [base=" + base + "]";
	}

}
