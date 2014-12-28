package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class EmbeddedFormControl extends JPanel implements
		IFieldControl {

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
		setBorder(BorderFactory.createTitledBorder(""));		
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

			@Override
			public String getName() {
				return getParentModificationSack().getName();
			}

			@Override
			public String toString() {
				return getParentModificationSack().toString();
			}

			@Override
			public void apply(IModification modif, boolean refreshView) {
				getParentModificationSack().apply(modif, refreshView);
			}

			@Override
			public void pushUndo(IModification undoModif) {
				getParentModificationSack().pushUndo(undoModif);
			}

			@Override
			public int getSize() {
				return getParentModificationSack().getSize();
			}

			@Override
			public int getRedoSize() {
				return getParentModificationSack().getRedoSize();
			}

			@Override
			public void undo(boolean refreshView) {
				getParentModificationSack().undo(refreshView);
			}

			@Override
			public int hashCode() {
				return getParentModificationSack().hashCode();
			}

			@Override
			public void redo(boolean refreshView) {
				getParentModificationSack().redo(refreshView);
			}

			@Override
			public void undoAll(boolean refreshView) {
				getParentModificationSack().undoAll(refreshView);
			}

			@Override
			public IModification[] getUndoModifications(ModificationStack.Order order) {
				return getParentModificationSack()
						.getUndoModifications(order);
			}

			@Override
			public void beginComposite() {
				getParentModificationSack().beginComposite();
			}

			@Override
			public void endComposite(String title, Order order) {
				getParentModificationSack().endComposite(title, order);
			}

			@Override
			public boolean equals(Object obj) {
				return getParentModificationSack().equals(obj);
			}

		};
		reflectionUI.getModificationStackByForm().put(subForm, newModifStack);
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

}
