package xy.reflect.ui.info.parameter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class HiddenNullableFacetParameterInfo extends ParameterInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IParameterInfo base;

	public HiddenNullableFacetParameterInfo(final ReflectionUI reflectionUI, final IParameterInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@Override
	public Object getDefaultValue() {
		Object result = super.getDefaultValue();
		if (result == null) {
			if (!isValueNullable()) {
				result = generateDefaultValueReplacement();
			}
		}
		return result;
	}

	public Object generateDefaultValueReplacement() {
		Object result;
		try {
			result = ReflectionUIUtils.createDefaultInstance(getType());
		} catch (Throwable t) {
			result = null;
		}
		if (result == null) {
			throw new ReflectionUIError(
					"Unable to generate a default parameter value");
		}
		return result;
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
		HiddenNullableFacetParameterInfo other = (HiddenNullableFacetParameterInfo) obj;
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
