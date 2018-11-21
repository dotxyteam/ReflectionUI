package xy.reflect.ui.undo;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldControlDataValueModification extends AbstractModification {

	protected IFieldControlData data;
	protected Object newValue;

	public FieldControlDataValueModification(final IFieldControlData data, final Object newValue) {
		this.data = data;
		this.newValue = newValue;
	}

	public static String getTitle(String targetCaption) {
		if ((targetCaption == null) || (targetCaption.length() == 0)) {
			return "";
		}
		return "Edit '" + targetCaption + "'";
	}

	@Override
	public String getTitle() {
		return getTitle(data.getCaption());
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(newValue);
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		return ReflectionUIUtils.getUndoJob(data, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		FieldControlDataValueModification other = (FieldControlDataValueModification) obj;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlDataValueModification [data=" + data + ", newValue=" + newValue + "]";
	}

}
