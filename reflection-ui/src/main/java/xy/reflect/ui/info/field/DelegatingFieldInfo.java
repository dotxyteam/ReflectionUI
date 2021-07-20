


package xy.reflect.ui.info.field;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Field proxy that delegates to another field that can be replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingFieldInfo implements IFieldInfo {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IFieldInfo getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

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

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return getDelegate().getAlternativeConstructors(object);
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return getDelegate().getAlternativeListItemConstructors(object);
	}

	public ITypeInfo getType() {
		return getDelegate().getType();
	}

	public Object getValue(Object object) {
		return getDelegate().getValue(object);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return getDelegate().hasValueOptions(object);
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

	@Override
	public boolean isTransient() {
		return getDelegate().isTransient();
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
	public void onControlVisibilityChange(Object object, boolean visible) {
		getDelegate().onControlVisibilityChange(object, visible);
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
		DelegatingFieldInfo other = (DelegatingFieldInfo) obj;
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingFieldInfo [delegate=" + getDelegateId() + "]";
	}

}
