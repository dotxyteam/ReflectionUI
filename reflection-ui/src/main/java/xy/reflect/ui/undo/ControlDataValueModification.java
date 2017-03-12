package xy.reflect.ui.undo;

import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.info.IInfo;

public class ControlDataValueModification extends AbstractModification {

	protected IControlData valueAccess;
	protected Object newValue;

	public ControlDataValueModification(final IControlData valueAccess, final Object newValue, IInfo target) {
		super(target);
		this.valueAccess = valueAccess;
		this.newValue = newValue;
	}

	public static String getTitle(IInfo target) {
		return "Edit '" + target.getCaption() + "'";
	}

	@Override
	public String getTitle() {
		return getTitle(target);
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				valueAccess.setValue(newValue);
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		Runnable result = valueAccess.getCustomUndoUpadteJob(newValue);
		if (result == null) {
			final Object oldValue = valueAccess.getValue();
			result = new Runnable() {
				@Override
				public void run() {
					valueAccess.setValue(oldValue);
				}
			};
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((valueAccess == null) ? 0 : valueAccess.hashCode());
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
		ControlDataValueModification other = (ControlDataValueModification) obj;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (valueAccess == null) {
			if (other.valueAccess != null)
				return false;
		} else if (!valueAccess.equals(other.valueAccess))
			return false;
		return true;
	}

}
