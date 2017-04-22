package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public class DefaultFieldControlData implements IFieldControlData {

	protected Object object;
	protected IFieldInfo field;

	public DefaultFieldControlData(Object object, IFieldInfo field) {
		this.object = object;
		this.field = field;
	}

	public Object getObject() {
		return object;
	}

	public IFieldInfo getField() {
		return field;
	}

	@Override
	public Object getValue() {
		return field.getValue(object);
	}

	@Override
	public void setValue(Object value) {
		field.setValue(object, value);
	}

	@Override
	public String getCaption() {
		return field.getCaption();
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object newValue) {
		return field.getCustomUndoUpdateJob(object, newValue);
	}

	@Override
	public boolean isGetOnly() {
		return field.isGetOnly();
	}

	public boolean isValueNullable() {
		return field.isValueNullable();
	}

	public String getNullValueLabel() {
		return field.getNullValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return field.getValueReturnMode();
	}

	@Override
	public ITypeInfo getType() {
		return field.getType();
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
