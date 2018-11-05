package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

public abstract class DelegatingFieldInfo implements IFieldInfo {

	protected abstract IFieldInfo getDelegate();

	public String getName() {
		return getDelegate().getName();
	}

	public String getCaption() {
		return getDelegate().getCaption();
	}

	public String getOnlineHelp() {
		return getDelegate().getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return getDelegate().getSpecificProperties();
	}

	public ITypeInfo getType() {
		return getDelegate().getType();
	}

	public Object getValue(Object object) {
		return getDelegate().getValue(object);
	}

	public Object[] getValueOptions(Object object) {
		return getDelegate().getValueOptions(object);
	}

	public void setValue(Object object, Object value) {
		getDelegate().setValue(object, value);
	}

	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return getDelegate().getNextUpdateCustomUndoJob(object, newValue);
	}

	public boolean isNullValueDistinct() {
		return getDelegate().isNullValueDistinct();
	}

	public boolean isGetOnly() {
		return getDelegate().isGetOnly();
	}

	public String getNullValueLabel() {
		return getDelegate().getNullValueLabel();
	}

	public ValueReturnMode getValueReturnMode() {
		return getDelegate().getValueReturnMode();
	}

	public InfoCategory getCategory() {
		return getDelegate().getCategory();
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

	public long getAutoUpdatePeriodMilliseconds() {
		return getDelegate().getAutoUpdatePeriodMilliseconds();
	}

	public boolean isHidden() {
		return getDelegate().isHidden();
	}

	public double getDisplayAreaHorizontalWeight() {
		return getDelegate().getDisplayAreaHorizontalWeight();
	}

	public double getDisplayAreaVerticalWeight() {
		return getDelegate().getDisplayAreaVerticalWeight();
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
		DelegatingFieldInfo other = (DelegatingFieldInfo) obj;
		if (getDelegate() == null) {
			if (other.getDelegate() != null)
				return false;
		} else if (!getDelegate().equals(other.getDelegate()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingFieldInfo [getDelegate()=" + getDelegate() + "]";
	}

}
