package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullStatusField extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected String fieldName;

	public NullStatusField(ReflectionUI reflectionUI, IFieldInfo base, String fieldName) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
	}

	protected Object valueToBoolean(Object value) {
		return value != null;
	}

	protected Object booleanTovalue(Boolean value) {
		if ((Boolean) value) {
			return ReflectionUIUtils.createDefaultInstance(super.getType());
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public Object getValue(Object object) {
		return valueToBoolean(super.getValue(object));
	}

	@Override
	public void setValue(Object object, Object value) {
		super.setValue(object, booleanTovalue((Boolean) value));
	}

	@Override
	public Runnable getCustomUndoUpdateJob(final Object object, final Object newValue) {
		final Object baseNewValue = booleanTovalue((Boolean) newValue);
		Runnable result = super.getCustomUndoUpdateJob(object, baseNewValue);
		if (result == null) {
			final Object baseOldValue = super.getValue(object);
			result = new Runnable() {
				@Override
				public void run() {
					base.setValue(object, baseOldValue);
				}
			};
		}
		return result;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(boolean.class));
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
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

}
