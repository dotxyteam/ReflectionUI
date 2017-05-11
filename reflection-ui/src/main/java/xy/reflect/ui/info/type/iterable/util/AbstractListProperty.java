package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;

public abstract class AbstractListProperty extends AbstractInfo implements IFieldInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

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
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
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
		if (!getName().equals(((AbstractListProperty) obj).getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ListProperty[name=" + getName() + "]";
	}

}
