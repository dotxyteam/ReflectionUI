package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.FieldInfoProxy;
import xy.reflect.ui.util.Accessor;

public class HiddenNullableFacetFieldInfoWrapper extends FieldInfoProxy {

	protected Accessor<Object> defaultValueAccessor;

	public HiddenNullableFacetFieldInfoWrapper(final ReflectionUI reflectionUI,
			final IFieldInfo wrapped) {
		this(wrapped, new Accessor<Object>() {
			@Override
			public Object get() {
				return reflectionUI.onTypeInstanciationRequest(
						wrapped.getType(), null, true);
			}
		});
	}

	public HiddenNullableFacetFieldInfoWrapper(IFieldInfo wrapped,
			Accessor<Object> defaultValueAccessor) {
		super(wrapped);
		this.defaultValueAccessor = defaultValueAccessor;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = defaultValueAccessor.get();
		}
		return result;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public int hashCode() {
		return defaultValueAccessor.hashCode();
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
		return defaultValueAccessor
				.equals(((HiddenNullableFacetFieldInfoWrapper) obj).defaultValueAccessor);
	}
}
