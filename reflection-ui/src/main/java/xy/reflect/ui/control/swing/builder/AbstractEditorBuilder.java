
package xy.reflect.ui.control.swing.builder;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.builder.DialogBuilder.RenderedDialog;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This is a base class for editor window factories.
 * 
 * @author olitank
 *
 */
public abstract class AbstractEditorBuilder extends AbstractEditorFormBuilder {

	protected ModificationStack createdFormModificationStack;
	protected EditorFrame createdFrame;
	protected RenderedDialog createdDialog;
	protected ITransactionInfo currentValueTransaction;

	/**
	 * @return the owner component of the editor window or null.
	 */
	protected abstract Component getOwnerComponent();

	@Override
	public EncapsulatedObjectFactory createEncapsulation() {
		EncapsulatedObjectFactory result = super.createEncapsulation();
		result.setTypeCaption(getCapsuleTypeCaption());
		result.setTypeIconImagePath(getCapsuleTypeIconImagePath());
		result.setTypeOnlineHelp(getCapsuleTypeOnlineHelp());
		result.setTypeModificationStackAccessible(isCapsuleTypeModificationStackAccessible());
		return result;
	}

	/**
	 * @return the path to the icon image of the capsule type.
	 */
	protected ResourcePath getCapsuleTypeIconImagePath() {
		return getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource()).getIconImagePath();
	}

	/**
	 * @return the online help of the capsule type.
	 */
	protected String getCapsuleTypeOnlineHelp() {
		return getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource()).getOnlineHelp();
	}

	/**
	 * @return true if and only if the undo/redo/etc features should be made
	 *         available.
	 */
	protected boolean isCapsuleTypeModificationStackAccessible() {
		if (isInReadOnlyMode()) {
			return false;
		}
		return getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource())
				.isModificationStackAccessible();
	}

	/**
	 * @return the caption of the capsule type.
	 */
	protected String getCapsuleTypeCaption() {
		return getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource()).getCaption();
	}

	/**
	 * @return whether the editing session should be cancellable (a cancel button
	 *         will be displayed) or not. Note that it only makes sense if an editor
	 *         dialog (not a frame) is created.
	 */
	protected boolean isDialogCancellable() {
		if (isInReadOnlyMode()) {
			return false;
		}
		Object capsule = getCapsule();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.buildTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(capsule));
		return encapsulatedObjectType.isModificationStackAccessible();
	}

	/**
	 * @return the title of the editor window.
	 */
	protected String getEditorWindowTitle() {
		Object capsule = getCapsule();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.buildTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(capsule));
		return encapsulatedObjectType.getCaption();
	}

	/**
	 * @return the icon image of the editor window.
	 */
	protected Image getEditorWindowIconImage() {
		ensureIsInitialized();
		Object capsule = getCapsule();
		Image result = getSwingRenderer().getObjectIconImage(capsule);
		if (result == null) {
			ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
			IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
			if (appInfo.getIconImagePath() != null) {
				result = SwingRendererUtils.loadImageThroughCache(appInfo.getIconImagePath(),
						ReflectionUIUtils.getErrorLogListener(reflectionUI));
			}
		}
		return result;
	}

	/**
	 * @return the text of the 'Cancel' button.
	 */
	protected String getCancelCaption() {
		return "Cancel";
	}

	/**
	 * @return the text of the 'OK' button.
	 */
	protected String getOKCaption() {
		return "OK";
	}

	/**
	 * @return the text of the 'Close' button.
	 */
	protected String getCloseCaption() {
		return "Close";
	}

	/**
	 * @return additional button bar controls that will be laid on the button bar.
	 */
	protected List<Component> getAdditionalButtonBarControls() {
		return Collections.emptyList();
	}

	/**
	 * @param editorForm The generated editor form.
	 * @return most button bar controls (the result includes
	 *         {@link #getAdditionalButtonBarControls()}, but not ok/cancel/close
	 *         buttons) that will be displayed on the button bar.
	 */
	protected List<Component> createMostButtonBarControls(Form editorForm) {
		List<Component> result = new ArrayList<Component>();
		List<Component> commonButtonBarControls = editorForm.createButtonBarControls();
		if (commonButtonBarControls != null) {
			result.addAll(commonButtonBarControls);
		}
		List<Component> additionalButtonBarComponents = getAdditionalButtonBarControls();
		if (additionalButtonBarComponents != null) {
			result.addAll(additionalButtonBarComponents);
		}
		return result;
	}

	/**
	 * @return the modification stack of the local value/object.
	 */
	public ModificationStack getModificationStack() {
		return createdFormModificationStack;
	}

	@Override
	public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
		Form result = super.createEditorForm(realTimeLinkWithParent, exclusiveLinkWithParent);
		createdFormModificationStack = result.getModificationStack();
		return result;
	}

	/**
	 * Creates and returns the editor frame.
	 * 
	 * @return the created editor frame.
	 */
	public JFrame createFrame() {
		createdFrame = new EditorFrame(this);
		return createdFrame;
	}

	/**
	 * @return the created editor frame.
	 */
	public EditorFrame getCreatedFrame() {
		return createdFrame;
	}

	/**
	 * Creates and shows the editor frame.
	 */
	public void createAndShowFrame() {
		getSwingRenderer().showFrame(createFrame());
	}

	/**
	 * @return the dialog builder used to build the editor dialog.
	 */
	protected DialogBuilder createDelegateDialogBuilder() {
		DialogBuilder dialogBuilder = getSwingRenderer().createDialogBuilder(getOwnerComponent());
		ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
		Object capsule = getCapsule();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(capsule));

		if (type.getFormButtonBackgroundColor() != null) {
			dialogBuilder
					.setClosingButtonBackgroundColor(SwingRendererUtils.getColor(type.getFormButtonBackgroundColor()));
		}

		if (type.getFormButtonForegroundColor() != null) {
			dialogBuilder
					.setClosingButtonForegroundColor(SwingRendererUtils.getColor(type.getFormButtonForegroundColor()));
		}

		if (type.getFormButtonBorderColor() != null) {
			dialogBuilder.setClosingButtonBorderColor(SwingRendererUtils.getColor(type.getFormButtonBorderColor()));
		}

		if (type.getFormButtonBackgroundImagePath() != null) {
			dialogBuilder.setClosingButtonBackgroundImage(SwingRendererUtils.loadImageThroughCache(
					type.getFormButtonBackgroundImagePath(), ReflectionUIUtils.getErrorLogListener(reflectionUI)));
		}
		return dialogBuilder;
	}

	/**
	 * Creates and returns the editor dialog.
	 * 
	 * @return the created editor dialog.
	 */
	public JDialog createDialog() {
		Form editorForm = createEditorForm(false, false);
		DialogBuilder dialogBuilder = createDelegateDialogBuilder();
		dialogBuilder.setContentComponent(editorForm);
		dialogBuilder.setTitle(getEditorWindowTitle());
		dialogBuilder.setIconImage(getEditorWindowIconImage());

		List<Component> buttonBarControls = new ArrayList<Component>(createMostButtonBarControls(editorForm));
		{
			if (isDialogCancellable()) {
				List<JButton> okCancelButtons = dialogBuilder.createStandardOKCancelDialogButtons(getOKCaption(),
						getCancelCaption());
				buttonBarControls.addAll(okCancelButtons);
			} else {
				buttonBarControls.add(dialogBuilder.createDialogClosingButton(getCloseCaption(), null));
			}
			dialogBuilder.setButtonBarControls(buttonBarControls);
		}
		final RenderedDialog dialog = dialogBuilder.createDialog();
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				initializeDialogModifications();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				finalizeDialogModifications();
				dialog.removeWindowListener(this);
			}

		});
		createdDialog = dialog;
		return dialog;
	}

	/**
	 * @return the created editor dialog.
	 */
	public JDialog getCreatedDialog() {
		return createdDialog;
	}

	/**
	 * Creates and shows the editor dialog. Note that the dialog is modal.
	 */
	public void createAndShowDialog() {
		createDialog();
		getSwingRenderer().showDialog(createdDialog, true);
	}

	/**
	 * Prepares a dialog edit session. Must be called before
	 * {@link #finalizeDialogModifications()}.
	 */
	public void initializeDialogModifications() {
		if (createdDialog == null) {
			throw new ReflectionUIError();
		}
		currentValueTransaction = null;
		{
			Object value = getCurrentValue();
			if (value != null) {
				ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
				ITypeInfo valueType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(value));
				currentValueTransaction = valueType.getTransaction(value);
			}
		}
		if (currentValueTransaction != null) {
			currentValueTransaction.begin();
		} else {
			createdFormModificationStack.setMaximumSize(Integer.MAX_VALUE);
		}
	}

	/**
	 * Terminates a dialog edit session by eventually updating the local
	 * value/object, its parent and their modification stacks according to the
	 * performed modifications and the current editor builder state and
	 * specifications. Must be preceded by a call to
	 * {@link #initializeDialogModifications()}.
	 */
	public void finalizeDialogModifications() {
		ModificationStack parentObjectModifStack = getParentModificationStack();
		ModificationStack valueModifStack = getModificationStack();
		ValueReturnMode valueReturnMode = getReturnModeFromParent();
		Object currentValue = getCurrentValue();
		boolean valueReplaced = isValueReplaced();
		boolean valueModifAccepted = shouldIntegrateNewObjectValue(currentValue)
				&& ((!isDialogCancellable()) || !isCancelled());
		boolean valueTransactionExecuted;
		if (currentValueTransaction != null) {
			if (valueModifAccepted) {
				currentValueTransaction.commit();
			} else {
				currentValueTransaction.rollback();
			}
			valueTransactionExecuted = true;
		} else {
			valueTransactionExecuted = false;
		}
		IModification committingModif;
		if (!canCommitToParent()) {
			committingModif = null;
		} else {
			committingModif = createCommittingModification(currentValue);
		}
		String parentObjectModifTitle = getParentModificationTitle();
		boolean parentObjectModifFake = isParentModificationFake();
		ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack, valueModifStack, valueModifAccepted,
				valueReturnMode, valueReplaced, valueTransactionExecuted, committingModif, parentObjectModifTitle,
				parentObjectModifFake, ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()),
				ReflectionUIUtils.getErrorLogListener(getSwingRenderer().getReflectionUI()));
		if (currentValueTransaction != null) {
			currentValueTransaction = null;
			if (parentObjectModifStack != null) {
				parentObjectModifStack.push(IModification.FAKE_MODIFICATION);
			}
		}
	}

	/**
	 * @return whether the user cancelled the editor dialog.
	 */
	public boolean isCancelled() {
		if (createdDialog == null) {
			return false;
		}
		if (!createdDialog.isDisposed()) {
			return false;
		}
		return !createdDialog.wasOkPressed();
	}

	/**
	 * Frame created using an implementation of {@link AbstractEditorBuilder}.
	 * 
	 * @author olitank
	 *
	 */
	public static class EditorFrame extends JFrame {

		private static final long serialVersionUID = 1L;

		protected WindowManager windowManager;
		protected AbstractEditorBuilder editorBuilder;
		protected boolean disposed = false;

		public EditorFrame(AbstractEditorBuilder editorBuilder) {
			this.editorBuilder = editorBuilder;
			this.windowManager = editorBuilder.getSwingRenderer().createWindowManager(this);
			installComponents();
		}

		@Override
		public void dispose() {
			if (disposed) {
				return;
			}
			disposed = true;
			uninstallComponents();
			this.windowManager = null;
			this.editorBuilder = null;
			super.dispose();
		}

		public boolean isDisposed() {
			return disposed;
		}

		protected void installComponents() {
			Form editorForm = editorBuilder.createEditorForm(false, false);
			windowManager.install(editorForm,
					new ArrayList<Component>(editorBuilder.createMostButtonBarControls(editorForm)),
					editorBuilder.getEditorWindowTitle(), editorBuilder.getEditorWindowIconImage());
		}

		protected void uninstallComponents() {
			windowManager.uninstall();
		}

		public AbstractEditorBuilder getEditorBuilder() {
			return editorBuilder;
		}

	}

}
