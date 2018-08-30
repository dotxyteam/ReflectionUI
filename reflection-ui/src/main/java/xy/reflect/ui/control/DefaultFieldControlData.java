package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public class DefaultFieldControlData implements IFieldControlData {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	public DefaultFieldControlData(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
	}

	public DefaultFieldControlData(ReflectionUI reflectionUI) {
		this(reflectionUI, null, IFieldInfo.NULL_FIELD_INFO);
	}

	public Object getObject() {
		return object;
	}

	public IFieldInfo getField() {
		return field;
	}

	@Override
	public Object getValue() {
		return field.getValue(getObject());
	}

	@Override
	public void setValue(Object value) {
		field.setValue(getObject(), value);
	}

	@Override
	public String getCaption() {
		return field.getCaption();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return field.getNextUpdateCustomUndoJob(getObject(), newValue);
	}

	@Override
	public ITypeInfo getType() {
		return field.getType();
	}

	@Override
	public boolean isGetOnly() {
		return field.isGetOnly();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return field.getValueReturnMode();
	}

	@Override
	public boolean isNullValueDistinct() {
		return field.isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return field.getNullValueLabel();
	}

	public boolean isFormControlMandatory() {
		return field.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return field.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return field.getFormControlFilter();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public ColorSpecification getFormForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormForegroundColor() != null) {
				return type.getFormForegroundColor();
			}
		}
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainForegroundColor() != null) {
			return appInfo.getMainForegroundColor();
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultFieldControlData other = (DefaultFieldControlData) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlData [object=" + object + ", field=" + field + "]";
	}

}
