package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractEditorBuilder extends AbstractEditorPanelBuilder {

	protected DialogBuilder dialogBuilder;
	protected JPanel editorPanel;
	protected boolean parentModificationStackImpacted = false;

	public abstract Component getOwnerComponent();

	public boolean isCancellable() {
		Object encapsualted = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsualted));
		return encapsulatedObjectType.isModificationStackAccessible();
	}

	@Override
	public String getEditorTitle() {
		return getEncapsulatedFieldType().getCaption();
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
		List<Component> commonToolbarControls = getSwingRenderer().createFormCommonToolbarControls(editorPanel);
		if (commonToolbarControls != null) {
			result.addAll(commonToolbarControls);
		}
		List<Component> additionalToolbarComponents = getAdditionalToolbarComponents();
		if (additionalToolbarComponents != null) {
			result.addAll(additionalToolbarComponents);
		}
		return result;
	}

	public void showFrame() {
		getSwingRenderer().showFrame(createFrame());
	}

	public JFrame createFrame() {
		editorPanel = createEditorPanel(true);
		JFrame frame = new JFrame();
		getSwingRenderer().setupWindow(frame, editorPanel, createAnyWindowToolbarControls(), getEditorTitle(),
				getObjectIconImage());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	protected DialogBuilder createDelegateDialogBuilder() {
		return getSwingRenderer().createDialogBuilder(getOwnerComponent());
	}

	public JDialog createDialog() {
		editorPanel = createEditorPanel(false);
		SwingRendererUtils.requestAnyComponentFocus(editorPanel, null, getSwingRenderer());
		dialogBuilder = createDelegateDialogBuilder();
		dialogBuilder.setContentComponent(editorPanel);
		dialogBuilder.setTitle(getEditorTitle());
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

	public void showDialog() {
		getSwingRenderer().showDialog(createDialog(), true);
		if (canPotentiallyModifyParent()) {
			impactParent();
		}
	}

	protected void impactParent() {
		ModificationStack parentModifStack = getParentModificationStack();
		if (parentModifStack == null) {
			return;
		}
		ModificationStack childModifStack = getSubObjectModificationStack();
		IInfo compositeModifTarget = getCumulatedModificationsTarget();
		ValueReturnMode childValueReturnMode = getObjectValueReturnMode();
		Object currentValue = getCurrentObjectValue();
		boolean childValueReplaced = isObjectValueReplaced();
		IModification commitModif;
		if (!canCommit()) {
			commitModif = null;
		} else {
			commitModif = createCommitModification(currentValue);
		}
		boolean childModifAccepted = isNewObjectValueAccepted(currentValue) && ((!isCancellable()) || wasOkPressed());
		String compositeModifTitle = getCumulatedModificationsTitle();
		parentModificationStackImpacted = ReflectionUIUtils.integrateSubModifications(
				getSwingRenderer().getReflectionUI(), parentModifStack, childModifStack, childModifAccepted,
				childValueReturnMode, childValueReplaced, commitModif, compositeModifTarget, compositeModifTitle);
	}

	public JDialog getCreatedEditor() {
		if (dialogBuilder == null) {
			return null;
		}
		return dialogBuilder.getCreatedDialog();
	}

	public boolean wasOkPressed() {
		if (dialogBuilder == null) {
			return false;
		}
		return dialogBuilder.wasOkPressed();
	}

	public ModificationStack getSubObjectModificationStack() {
		if (editorPanel == null) {
			return null;
		}
		return getSwingRenderer().getModificationStackByForm().get(editorPanel);
	}

	public boolean isParentModificationStackImpacted() {
		return parentModificationStackImpacted;
	}

}
