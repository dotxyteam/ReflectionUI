package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public abstract class DelegatingFieldControlData implements IFieldControlData {

	protected abstract IFieldControlData getDelegate();

	public Object getValue() {
		return getDelegate().getValue();
	}

	public void setValue(Object value) {
		getDelegate().setValue(value);
	}

	public String getCaption() {
		return getDelegate().getCaption();
	}

	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return getDelegate().getNextUpdateCustomUndoJob(newValue);
	}

	public ITypeInfo getType() {
		return getDelegate().getType();
	}

	public boolean isGetOnly() {
		return getDelegate().isGetOnly();
	}

	public ValueReturnMode getValueReturnMode() {
		return getDelegate().getValueReturnMode();
	}

	public boolean isNullValueDistinct() {
		return getDelegate().isNullValueDistinct();
	}

	public String getNullValueLabel() {
		return getDelegate().getNullValueLabel();
	}

	public boolean isFormControlMandatory() {
		return getDelegate().isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return getDelegate().isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return getDelegate().getFormControlFilter();
	}

	public Map<String, Object> getSpecificProperties() {
		return getDelegate().getSpecificProperties();
	}

	public ColorSpecification getForegroundColor() {
		return getDelegate().getForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return getDelegate().getBorderColor();
	}

	public Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor) {
		return getDelegate().createValue(typeToInstanciate, selectableConstructor);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDelegate() == null) ? 0 : getDelegate().hashCode());
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
		DelegatingFieldControlData other = (DelegatingFieldControlData) obj;
		if (getDelegate() == null) {
			if (other.getDelegate() != null)
				return false;
		} else if (!getDelegate().equals(other.getDelegate()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingFieldControlData [delegate=" + getDelegate() + "]";
	}

}
