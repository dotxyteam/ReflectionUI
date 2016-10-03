package xy.reflect.ui.undo;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;

public class SetFieldValueModification implements IModification {

	protected Object object;
	protected IFieldInfo field;
	protected Object value;
	protected ReflectionUI reflectionUI;

	public SetFieldValueModification(ReflectionUI reflectionUI, Object object, IFieldInfo field, Object value) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.value = value;
	}

	@Override
	public IInfo getTarget() {
		return field;
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public IModification applyAndGetOpposite() {
		Object currentValue = field.getValue(object);
		final SetFieldValueModification currentModif = this;
		SetFieldValueModification opposite = new SetFieldValueModification(reflectionUI, object, field, currentValue) {
			@Override
			public String getTitle() {
				return ModificationStack.getUndoTitle(currentModif.getTitle());
			}
		};
		field.setValue(object, value);
		return opposite;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SetFieldValueModification other = (SetFieldValueModification) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String getTitle() {
		return "Edit '" + field.getCaption() + "'";
	}

}
