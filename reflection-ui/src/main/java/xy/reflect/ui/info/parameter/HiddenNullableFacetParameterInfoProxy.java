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
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return base.equals(((HiddenNullableFacetParameterInfoProxy) obj).base);
	}
}
