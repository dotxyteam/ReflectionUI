package xy.reflect.ui.info.type.iterable;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public class ListPropertyProxy implements IListProperty {

	protected IListProperty base;

	public ListPropertyProxy(IListProperty base) {
		super();
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public Object getRootListValue() {
		return base.getRootListValue();
	}

	public List<ItemPosition> getPostSelection() {
		return base.getPostSelection();
	}

	public boolean isEnabled() {
		return base.isEnabled();
	}

	public ITypeInfo getType() {
		return base.getType();
	}

	public Object getValue(Object object) {
		return base.getValue(object);
	}

	public Object[] getValueOptions(Object object) {
		return base.getValueOptions(object);
	}

	public void setValue(Object object, Object value) {
		base.setValue(object, value);
	}

	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return base.getNextUpdateCustomUndoJob(object, newValue);
	}

	public boolean isNullValueDistinct() {
		return base.isNullValueDistinct();
	}

	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	public String getNullValueLabel() {
		return base.getNullValueLabel();
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public InfoCategory getCategory() {
		return base.getCategory();
	}

	public boolean isFormControlMandatory() {
		return base.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return base.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return base.getFormControlFilter();
	}

	public long getAutoUpdatePeriodMilliseconds() {
		return base.getAutoUpdatePeriodMilliseconds();
	}

	public boolean isHidden() {
		return base.isHidden();
	}

	public double getDisplayAreaHorizontalWeight() {
		return base.getDisplayAreaHorizontalWeight();
	}

	public double getDisplayAreaVerticalWeight() {
		return base.getDisplayAreaVerticalWeight();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		ListPropertyProxy other = (ListPropertyProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ListPropertyProxy [name=" + getName() + ", base=" + base + "]";
	}

}
