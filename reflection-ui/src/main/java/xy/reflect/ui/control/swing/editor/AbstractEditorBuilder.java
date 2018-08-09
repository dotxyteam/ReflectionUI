package xy.reflect.ui.control.swing.editor;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.Form;
import xy.reflect.ui.control.swing.WindowManager;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractEditorBuilder extends AbstractEditFormBuilder {

	protected DialogBuilder dialogBuilder;
	protected Form createdEditForm;
	protected JFrame createdFrame;
	protected boolean parentModificationStackImpacted = false;

	public abstract Component getOwnerComponent();

	public boolean isCancellable() {
		Object encapsualted = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsualted));
		return encapsulatedObjectType.isModificationStackAccessible();
	}

	public String getEditorWindowTitle() {
		Object encapsulatedObject = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedObject));
		return encapsulatedObjectType.getCaption();
	}

	public Image getObjectIconImage() {
		ensureObjectValueIsInitialized();
		return getSwingRenderer().getObjectIconImage(initialObjectValue);
	}

	public String getCancelCaption() {
		return "Cancel";
	}

	public String getOKCaption() {
		return "OK";
	}

	public String getCloseCaption() {
		return "Close";
	}

	public List<Component> getAdditionalToolbarComponents() {
		return Collections.emptyList();
	}

	protected List<? extends Component> createAnyWindowToolbarControls() {
		List<Component> result = new ArrayList<Component>();
		List<Component> commonToolbarControls = createdEditForm.createFormToolbarControls();
		if (commonToolbarControls != null) {
			result.addAll(commonToolbarControls);
		}
		List<Component> additionalToolbarComponents = getAdditionalToolbarComponents();
		if (additionalToolbarComponents != null) {
			result.addAll(additionalToolbarComponents);
		}
		return result;
	}

	public JFrame createFrame() {
		createdEditForm = createForm(false, false);
		createdFrame = new JFrame();
		WindowManager windowManager = getSwingRenderer().createWindowManager(createdFrame);
		windowManager.set(createdEditForm, createAnyWindowToolbarControls(),
				getEditorWindowTitle(), getObjectIconImage());
		createdFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return createdFrame;
	}

	public JFrame getCreatedFrame() {
		return createdFrame;
	}

	public void showFrame() {
		getSwingRenderer().showFrame(createFrame());
	}

	protected DialogBuilder createDelegateDialogBuilder() {
		return getSwingRenderer().getDialogBuilder(getOwnerComponent());
	}

	public JDialog createDialog() {
		createdEditForm = createForm(false, false);
		dialogBuilder = createDelegateDialogBuilder();
		dialogBuilder.setContentComponent(createdEditForm);
		dialogBuilder.setTitle(getEditorWindowTitle());
		dialogBuilder.setIconImage(getObjectIconImage());

		List<Component> toolbarControls = new ArrayList<Component>(createAnyWindowToolbarControls());
		{
			if (isCancellable()) {
				List<JButton> okCancelButtons = dialogBuilder.createStandardOKCancelDialogButtons(getOKCaption(),
						getCancelCaption());
				toolbarControls.addAll(okCancelButtons);
			} else {
				toolbarControls.add(dialogBuilder.createDialogClosingButton(getCloseCaption(), null));
			}
			dialogBuilder.setToolbarComponents(toolbarControls);
		}
		return dialogBuilder.createDialog();
	}

	public JDialog getCreatedDialog() {
		if (dialogBuilder == null) {
			return null;
		}
		return dialogBuilder.getCreatedDialog();
	}

	public void showDialog() {
		getSwingRenderer().showDialog(createDialog(), true);
		if (hasParentObject()) {
			if (canPotentiallyModifyParentObject()) {
				impactParent();
			}
		} else {
			if (isCancelled()) {
				ModificationStack modifStack = getObjectModificationStack();
				modifStack.undoAll();
				if (modifStack.wasInvalidated()) {
					getSwingRenderer().getReflectionUI()
							.logDebug("WARNING: Cannot undo completely invalidated modification stack: " + modifStack);
				}
			}
		}
	}

	public Form getCreatedEditForm() {
		return createdEditForm;
	}

	protected void impactParent() {
		ModificationStack parentObjectModifStack = getParentObjectModificationStack();
		if (parentObjectModifStack == null) {
			return;
		}
		ModificationStack valueModifStack = getObjectModificationStack();
		IInfo editSessionTarget = getCumulatedModificationsTarget();
		ValueReturnMode valueReturnMode = getObjectValueReturnMode();
		Object currentValue = getCurrentObjectValue();
		boolean valueReplaced = isObjectValueReplaced();
		IModification commitModif;
		if (!canCommit()) {
			commitModif = null;
		} else {
			commitModif = createCommitModification(currentValue);
		}
		boolean valueModifAccepted = shouldAcceptNewObjectValue(currentValue) && ((!isCancellable()) || !isCancelled());
		String editSessionTitle = getCumulatedModificationsTitle();
		parentModificationStackImpacted = ReflectionUIUtils.finalizeSeparateObjectValueEditSession(
				parentObjectModifStack, valueModifStack, valueModifAccepted, valueReturnMode, valueReplaced,
				commitModif, editSessionTarget, editSessionTitle,
				ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()));
	}

	public boolean isCancelled() {
		if (dialogBuilder == null) {
			return false;
		}
		return !dialogBuilder.wasOkPressed();
	}

	public ModificationStack getObjectModificationStack() {
		if (createdEditForm == null) {
			return null;
		}
		return createdEditForm.getModificationStack();
	}

	public boolean isParentModificationStackImpacted() {
		return parentModificationStackImpacted;
	}

}
