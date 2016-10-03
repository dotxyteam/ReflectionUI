package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SetFieldValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class EmbeddedFormControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;
	protected int lastFocusedFieldControlPlaceHolderIndex = -1;
	protected ITypeInfo lastFocusSubFormObjectType;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
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
				onceContainingWindowDisplayed();
			}
		});
		refreshUI();
	}

	public JPanel getSubForm() {
		return subForm;
	}

	protected void onceContainingWindowDisplayed() {
		forwardModificationsToParentForm(subForm);
	}

	@Override
	public void requestFocus() {
		if (subForm != null) {
			if (lastFocusedFieldControlPlaceHolderIndex != -1) {
				ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
				if (lastFocusSubFormObjectType.equals(subFormObjectType)) {
					List<FieldControlPlaceHolder> fieldControlPlaceHolders = swingRenderer
							.getAllFieldControlPlaceHolders(subForm);
					fieldControlPlaceHolders.get(lastFocusedFieldControlPlaceHolderIndex).requestFocus();
				}
			}

		}
	}

	protected void forwardModificationsToParentForm(JPanel subForm) {
		final ModificationStack parentModifStack = SwingRendererUtils.findModificationStack(EmbeddedFormControl.this,
				swingRenderer);
		swingRenderer.getModificationStackByForm().put(subForm, new ModificationStack(null) {

			@Override
			public void pushUndo(IModification undoModif) {
				saveFocusInformation();
				if (shouldUpdateField()) {
					parentModifStack.beginComposite();
					parentModifStack.pushUndo(undoModif);
					parentModifStack.apply(new SetFieldValueModification(swingRenderer.getReflectionUI(), object, field,
							subFormObject));
					parentModifStack.endComposite(field, ModificationStack.getUndoTitle(undoModif.getTitle()), UndoOrder.FIFO);
				} else {
					parentModifStack.pushUndo(undoModif);
				}
			}

			@Override
			public void beginComposite() {
				parentModifStack.beginComposite();
			}

			@Override
			public void endComposite(IInfo target, String title, UndoOrder order) {
				parentModifStack.endComposite(field, title, order);
			}

			@Override
			public void cancelComposite() {
				parentModifStack.cancelComposite();
			}

			@Override
			public void invalidate() {
				saveFocusInformation();
				if (shouldUpdateField()) {
					field.setValue(object, subFormObject);
				}
				parentModifStack.invalidate();
			}

		});
	}

	protected boolean shouldUpdateField() {
		Object oldValue = field.getValue(object);
		return (!field.isGetOnly())
				&& (!ReflectionUIUtils.equals(swingRenderer.getReflectionUI(), oldValue, subFormObject));
	}

	protected void saveFocusInformation() {
		lastFocusSubFormObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
		lastFocusedFieldControlPlaceHolderIndex = swingRenderer.getFocusedFieldControlPaceHolderIndex(subForm);
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
			subForm = swingRenderer.createObjectForm(subFormObject, IInfoCollectionSettings.DEFAULT);
			add(subForm, BorderLayout.CENTER);
			swingRenderer.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = field.getValue(object);
			if (ReflectionUIUtils.equals(swingRenderer.getReflectionUI(), newSubFormObject, subFormObject)) {
				swingRenderer.refreshAllFieldControls(subForm, false);
			} else {
				remove(subForm);
				subForm = null;
				refreshUI();
				forwardModificationsToParentForm(subForm);
			}
		}
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

}
