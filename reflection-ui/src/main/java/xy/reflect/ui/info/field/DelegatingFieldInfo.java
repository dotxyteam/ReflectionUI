
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

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public String getCaption() {
		return getDelegate().getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return getDelegate().getOnlineHelp();
	}

	@Override
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

	@Override
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

	@Override
	public Object[] getValueOptions(Object object) {
		return getDelegate().getValueOptions(object);
	}

	@Override
	public void setValue(Object object, Object value) {
		getDelegate().setValue(object, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return getDelegate().getNextUpdateCustomUndoJob(object, newValue);
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
		return getDelegate().getPreviousUpdateCustomRedoJob(object, newValue);
	}

	@Override
	public boolean isNullValueDistinct() {
		return getDelegate().isNullValueDistinct();
	}

	@Override
	public boolean isGetOnly() {
		return getDelegate().isGetOnly();
	}

	@Override
	public boolean isTransient() {
		return getDelegate().isTransient();
	}

	@Override
	public String getNullValueLabel() {
		return getDelegate().getNullValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return getDelegate().getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return getDelegate().getCategory();
	}

	@Override
	public boolean isFormControlMandatory() {
		return getDelegate().isFormControlMandatory();
	}

	@Override
	public boolean isFormControlEmbedded() {
		return getDelegate().isFormControlEmbedded();
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return getDelegate().getFormControlFilter();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return getDelegate().getAutoUpdatePeriodMilliseconds();
	}

	@Override
	public boolean isHidden() {
		return getDelegate().isHidden();
	}

	@Override
	public boolean isRelevant(Object object) {
		return getDelegate().isRelevant(object);
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return getDelegate().getDisplayAreaHorizontalWeight();
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return getDelegate().getDisplayAreaVerticalWeight();
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return getDelegate().isDisplayAreaHorizontallyFilled();
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return getDelegate().isDisplayAreaVerticallyFilled();
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
		getDelegate().onControlVisibilityChange(object, visible);
	}

	@Override
	public boolean isValueValidityDetectionEnabled() {
		return getDelegate().isValueValidityDetectionEnabled();
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
