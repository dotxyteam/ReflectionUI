package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class EmbeddedFormControl extends JPanel implements
		ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;

	public EmbeddedFormControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());
		JPanel subForm = createSubForm();
		if (subForm != null) {
			add(subForm, BorderLayout.CENTER);
		}
	}

	protected JPanel createSubForm() {
		JPanel subForm = reflectionUI.createObjectForm(field.getValue(object));
		if (subForm != null) {
			connectSubFormToParentModificationStack(subForm);
		}
		return subForm;
	}

	protected void connectSubFormToParentModificationStack(JPanel subForm) {
		ModificationStack newModifStack = new ModificationStack(null) {

			protected ModificationStack getParentModificationSack() {
				ModificationStack parentModifStack = ReflectionUIUtils
						.findModificationStack(EmbeddedFormControl.this,
								reflectionUI);
				return parentModifStack;
			}

			public String getName() {
				return getParentModificationSack().getName();
			}

			public String toString() {
				return getParentModificationSack().toString();
			}

			public void apply(IModification modif, boolean refreshView) {
				getParentModificationSack().apply(modif, refreshView);
			}

			public void pushUndo(IModification undoModif) {
				getParentModificationSack().pushUndo(undoModif);
			}

			public int getSize() {
				return getParentModificationSack().getSize();
			}

			public int getRedoSize() {
				return getParentModificationSack().getRedoSize();
			}

			public void undo(boolean refreshView) {
				getParentModificationSack().undo(refreshView);
			}

			public int hashCode() {
				return getParentModificationSack().hashCode();
			}

			public void redo(boolean refreshView) {
				getParentModificationSack().redo(refreshView);
			}

			public void undoAll(boolean refreshView) {
				getParentModificationSack().undoAll(refreshView);
			}

			public IModification[] getUndoModificationsInPopOrder() {
				return getParentModificationSack()
						.getUndoModificationsInPopOrder();
			}

			public void beginComposite() {
				getParentModificationSack().beginComposite();
			}

			public void endComposite(String title) {
				getParentModificationSack().endComposite(title);
			}

			public boolean equals(Object obj) {
				return getParentModificationSack().equals(obj);
			}

		};
		reflectionUI.getModificationStackByForm().put(subForm, newModifStack);
	}

	@Override
	public void showCaption() {
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
	}

}
