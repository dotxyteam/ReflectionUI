


package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Field control data that delegates to another field control data that can be
 * replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingFieldControlData implements IFieldControlData {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IFieldControlData getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

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

	public ColorSpecification getLabelForegroundColor() {
		return getDelegate().getLabelForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return getDelegate().getBorderColor();
	}

	public ResourcePath getButtonBackgroundImagePath() {
		return getDelegate().getButtonBackgroundImagePath();
	}

	public ColorSpecification getButtonBackgroundColor() {
		return getDelegate().getButtonBackgroundColor();
	}

	public ColorSpecification getButtonForegroundColor() {
		return getDelegate().getButtonForegroundColor();
	}

	public ColorSpecification getButtonBorderColor() {
		return getDelegate().getButtonBorderColor();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDelegateId() == null) ? 0 : getDelegateId().hashCode());
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
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingFieldControlData [delegate=" + getDelegateId() + "]";
	}

}
