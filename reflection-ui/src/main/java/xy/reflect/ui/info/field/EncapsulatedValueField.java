package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

public class EncapsulatedValueField extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected EncapsulatedObjectFactory encapsulation;

	public EncapsulatedValueField(ReflectionUI reflectionUI, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.encapsulation = createEncapsulation();
	}

	protected EncapsulatedObjectFactory createEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(reflectionUI, super.getType(), getCaption(),
				"");
		result.setFieldGetOnly(super.isGetOnly());
		result.setFieldNullable(super.isNullable());
		result.setFieldNullValueLabel(super.getNullValueLabel());
		result.setFieldSpecificProperties(super.getSpecificProperties());
		result.setFieldValueReturnMode(super.getValueReturnMode());
		return result;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(encapsulation.getInstanceTypeInfoSource());
	}

	@Override
	public Object getValue(final Object object) {
		return encapsulation.getInstance(new Accessor<Object>() {

			@Override
			public Object get() {
				return EncapsulatedValueField.this.base.getValue(object);
			}

			@Override
			public void set(Object t) {
				EncapsulatedValueField.this.base.setValue(object, t);
			}

		});
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return "EncapsulatedValueField [base=" + base + "]";
	}

}
