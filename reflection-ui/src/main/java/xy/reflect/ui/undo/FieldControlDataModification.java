


package xy.reflect.ui.undo;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Modification that updates a field control data value.
 * 
 * @author olitank
 *
 */
public class FieldControlDataModification extends AbstractModification {

	protected IFieldControlData data;
	protected Object newValue;

	public FieldControlDataModification(final IFieldControlData data, final Object newValue) {
		check(data);
		this.data = data;
		this.newValue = newValue;
	}

	protected void check(IFieldControlData data) {
		if (Boolean.TRUE.equals(data.getSpecificProperties()
				.get(FieldControlPlaceHolder.CONTROL_AUTO_MANAGEMENT_ENABLED_PROPERTY_KEY))) {
			throw new ReflectionUIError("A " + FieldControlDataModification.class.getSimpleName()
					+ " must not be constructed with a control data that has the control auto-management property enabled."
					+ "\n" + "It ensures that a " + FieldControlDataModification.class.getSimpleName()
					+ " is not already constructed by the " + FieldControlPlaceHolder.class.getSimpleName()
					+ " undo-management proxy for the same control data.");
		}
	}

	public static String getTitle(String fieldCaption) {
		if ((fieldCaption == null) || (fieldCaption.length() == 0)) {
			return null;
		}
		return "Edit '" + fieldCaption + "'";
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
		return ReflectionUIUtils.getNextUpdateUndoJob(data, newValue);
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
		FieldControlDataModification other = (FieldControlDataModification) obj;
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
		return "FieldControlDataModification [data=" + data + ", newValue=" + newValue + "]";
	}

}
