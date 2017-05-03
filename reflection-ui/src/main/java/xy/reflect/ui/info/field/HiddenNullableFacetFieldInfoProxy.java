package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class HiddenNullableFacetFieldInfoProxy extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo base;

	public HiddenNullableFacetFieldInfoProxy(final ReflectionUI reflectionUI, final IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@Override
	public Object getValue(final Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			if (!isValueNullable()) {
				result = generateNullReplacementValue();
			}
		}
		return result;
	}

	public Object generateNullReplacementValue() {
		try {
			return ReflectionUIUtils.createDefaultInstance(getType());
		} catch (Throwable t) {
			throw new ReflectionUIError("Unable to generate a default value for the field: '" + getType().getName()
					+ " " + getName() + "'");
		}
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
