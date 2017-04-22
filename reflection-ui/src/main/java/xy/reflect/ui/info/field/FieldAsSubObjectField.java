package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

public class FieldAsSubObjectField extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected String fieldName;
protected EncapsulatedObjectFactory encapsulation;
	
	public FieldAsSubObjectField(ReflectionUI reflectionUI, IFieldInfo base, String fieldName) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
		this.encapsulation = createEncapsulation();
	}

	protected EncapsulatedObjectFactory createEncapsulation() {
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(reflectionUI, super.getType(), getCaption(),
				"") {
			@Override
			protected Object[] getFieldValueOptions(Object object) {
				return FieldAsSubObjectField.this.base.getValueOptions(object);
			}
			
			@Override
			protected Runnable getFieldCustomUndoUpdateJob(Object object, Object value) {
				return FieldAsSubObjectField.this.base.getCustomUndoUpdateJob(object, value);
			}
			
			
		};
		result.setFieldName(super.getName());
		result.setFieldGetOnly(super.isGetOnly());
		result.setFieldValueReturnMode(super.getValueReturnMode());
		result.setFieldNullable(super.isValueNullable());
		result.setFieldNullValueLabel(super.getNullValueLabel());
		result.setFieldFormControlEmbedded(super.isFormControlEmbedded());
		result.setFieldFormControlFilter(super.getFormControlFilter());
		result.setFieldFormControlMandatory(super.isFormControlMandatory());
		result.setFieldOnlineHelp(super.getOnlineHelp());
		result.setFieldOnlineHelp(super.getOnlineHelp());
		result.setFieldTypeSpecificities(super.getTypeSpecificities());
		return result;
	}
	
	

	@Override
	public String getName() {
		return fieldName;
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
				return FieldAsSubObjectField.this.base.getValue(object);
			}

			@Override
			public void set(Object t) {
				FieldAsSubObjectField.this.base.setValue(object, t);
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
	public boolean isValueNullable() {
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
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public String getOnlineHelp() {
		return null;
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
