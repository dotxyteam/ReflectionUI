
package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class exists as convenience for creating {@link IDynamicListProperty}
 * objects. The implemented methods have a default behavior.
 * 
 * @author olitank
 *
 */
public abstract class AbstractDynamicListProperty extends AbstractInfo implements IDynamicListProperty {

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public DisplayMode getDisplayMode() {
		return DisplayMode.TOOLBAR_AND_CONTEXT_MENU;
	}

	@Override
	public List<ItemPosition> getPostSelection() {
		return null;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 0.0;
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return true;
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return false;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
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
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return true;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public boolean isControlValueValiditionEnabled() {
		return false;
	}

	@Override
	public IValidationJob getValueAbstractFormValidationJob(Object object) {
		return null;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!getName().equals(((AbstractDynamicListProperty) obj).getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ListProperty[name=" + getName() + "]";
	}

}
