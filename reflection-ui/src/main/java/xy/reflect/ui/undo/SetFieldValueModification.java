package xy.reflect.ui.undo;

import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;

public class SetFieldValueModification implements IModification {

	protected Object object;
	protected IFieldInfo field;
	protected Object value;
	protected ReflectionUI reflectionUI;

	public SetFieldValueModification(ReflectionUI reflectionUI,
			Object object, IFieldInfo field, Object value) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.value = value;
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public IModification applyAndGetOpposite(boolean refreshView) {
		Object currentValue = field.getValue(object);
		final SetFieldValueModification currentModif = this;
		SetFieldValueModification opposite = new SetFieldValueModification(
				reflectionUI, object, field, currentValue) {
			@Override
			public String getTitle() {
				return ModificationStack.getUndoTitle(currentModif.getTitle());
			}
		};
		field.setValue(object, value);
		if (refreshView) {
			for (JPanel form : reflectionUI.getSwingRenderer().getForms(object)) {
				reflectionUI.getSwingRenderer().refreshFieldControlsByName(form,
						field.getName());
			}
		}
		return opposite;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return "Edit '" + field.getCaption() + "'";
	}

}

