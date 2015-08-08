package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.ReflectionUI.FieldControlPlaceHolder;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class EmbeddedFormControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;
	protected int lastFocusedFieldControlPlaceHolderIndex;
	protected ITypeInfo lastFocusSubFormObjectType;

	public EmbeddedFormControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				whenContainingWindowDisplayed();
			}
		});
		refreshUI();
	}

	protected void whenContainingWindowDisplayed() {
		forwardUpdatesToParentForm(subForm);
	}

	@Override
	public void requestFocus() {
		if (subForm != null) {
			if (lastFocusedFieldControlPlaceHolderIndex != -1) {
				ITypeInfo subFormObjectType = reflectionUI
						.getTypeInfo(reflectionUI
								.getTypeInfoSource(subFormObject));
				if (lastFocusSubFormObjectType.equals(subFormObjectType)) {
					List<FieldControlPlaceHolder> fieldControlPlaceHolders = reflectionUI
							.getAllFieldControlPlaceHolders(subForm);
					fieldControlPlaceHolders.get(
							lastFocusedFieldControlPlaceHolderIndex)
							.requestFocus();
				}
			}

		}
	}

	protected void forwardUpdatesToParentForm(JPanel subForm) {
		final ModificationStack parentModifStack = ReflectionUIUtils
				.findModificationStack(EmbeddedFormControl.this, reflectionUI);
		reflectionUI.getModificationStackByForm().put(subForm,
				new ModificationStack(null) {

					@Override
					public void pushUndo(IModification undoModif) {
						updateFocusInformation();
						Object oldValue = field.getValue(object);
						if (reflectionUI.equals(oldValue, subFormObject)) {
							parentModifStack.pushUndo(undoModif);
						} else {
							parentModifStack.beginComposite();
							parentModifStack.pushUndo(undoModif);
							field.setValue(object, subFormObject);
							parentModifStack.endComposite(ModificationStack
									.getUndoTitle(undoModif.getTitle()),
									UndoOrder.FIFO);
						}
					}

					@Override
					public void beginComposite() {
						parentModifStack.beginComposite();
					}

					@Override
					public void endComposite(String title, UndoOrder order) {
						parentModifStack.endComposite(title, order);
					}

					@Override
					public void invalidate() {
						updateFocusInformation();
						Object oldValue = field.getValue(object);
						if (!reflectionUI.equals(oldValue, subFormObject)) {
							field.setValue(object, subFormObject);
						}
						parentModifStack.invalidate();
					}

				});
	}

	protected void updateFocusInformation() {
		lastFocusSubFormObjectType = reflectionUI
				.getTypeInfo(reflectionUI
						.getTypeInfoSource(subFormObject));
		lastFocusedFieldControlPlaceHolderIndex = reflectionUI.getFocusedFieldControlPaceHolderIndex(subForm);
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
		if (subForm == null) {
			subFormObject = field.getValue(object);
			subForm = reflectionUI.createObjectForm(subFormObject);
			add(subForm, BorderLayout.CENTER);
			reflectionUI.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = field.getValue(object);
			if (reflectionUI.equals(newSubFormObject, subFormObject)) {
				reflectionUI.refreshAllFieldControls(subForm);
				reflectionUI.handleComponentSizeChange(this);
			} else {
				remove(subForm);
				subForm = null;
				refreshUI();
				forwardUpdatesToParentForm(subForm);
			}
		}
		return true;
	}

}
