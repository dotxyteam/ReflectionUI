package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.FieldInfoProxy;

public class HiddenNullableFacetFieldInfoProxy extends FieldInfoProxy {

	private ReflectionUI reflectionUI;
	private IFieldInfo base;

	public HiddenNullableFacetFieldInfoProxy(final ReflectionUI reflectionUI,
			final IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = getDefaultValue();
			setValue(object, result);
		}
		return result;
	}

	protected Object getDefaultValue() {
		Object result = reflectionUI.onTypeInstanciationRequest(null,
				base.getType(), true, true);
		if (result == null) {
			throw new AssertionError(
					"Failed to instanciate automatically the value of the field '"
							+ base
							+ "': Could not instanciate the field type '"
							+ base.getType() + "'");
		}
		return result;
	}

	@Override
	public boolean isNullable() {
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
		return base
				.equals(((HiddenNullableFacetFieldInfoProxy) obj).base);
	}
}
