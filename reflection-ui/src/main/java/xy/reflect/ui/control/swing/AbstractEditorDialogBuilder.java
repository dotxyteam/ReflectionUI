package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractEditorDialogBuilder extends AbstractEditorPanelBuilder {

	protected DialogBuilder delegate;
	protected JPanel editorPanel;
	protected boolean parentModificationStackImpacted = false;

	public abstract Component getOwnerComponent();

	public boolean isCancellable() {
		return getEncapsulatedObjectType().isModificationStackAccessible();
	}

	@Override
	public String getEditorTitle() {
		return getSwingRenderer().getObjectTitle(getCurrentObjectValue());
	}

	public Image getObjectIconImage() {
		return getSwingRenderer().getObjectIconImage(getCurrentObjectValue());
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

	protected DialogBuilder createDelegateDialogBuilder() {
		return new DialogBuilder(getSwingRenderer(), getOwnerComponent());
	}

	public JDialog createDialog() {
		Object encapsulated = getEncapsulatedObject();
		editorPanel = getSwingRenderer().createForm(encapsulated);
		
		delegate = createDelegateDialogBuilder();
		delegate.setContentComponent(editorPanel);
		delegate.setTitle(getEditorTitle());
		delegate.setIconImage(getObjectIconImage());

		List<Component> toolbarControls = new ArrayList<Component>();
		List<Component> commonToolbarControls = getSwingRenderer().createFormCommonToolbarControls(editorPanel);
		if (commonToolbarControls != null) {
			toolbarControls.addAll(commonToolbarControls);
		}
		List<Component> additionalToolbarComponents = getAdditionalToolbarComponents();
		if (additionalToolbarComponents != null) {
			toolbarControls.addAll(additionalToolbarComponents);
		}
		if (isCancellable()) {
			List<JButton> okCancelButtons = delegate.createStandardOKCancelDialogButtons(getOKCaption(),
					getCancelCaption());
			toolbarControls.addAll(okCancelButtons);
		} else {
			toolbarControls.add(delegate.createDialogClosingButton(getCloseCaption(), null));
		}
		delegate.setToolbarComponents(toolbarControls);

		return delegate.createDialog();
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
		IModification commitModif;
		if (!canCommit()) {
			commitModif = null;
		} else {
			commitModif = createCommitModification(encapsulatedObjectValueAccessor.get());
		}
		boolean childModifAccepted = (!isCancellable()) || wasOkPressed();
		String compositeModifTitle = getCumulatedModificationsTitle();
		parentModificationStackImpacted = ReflectionUIUtils.integrateSubModifications(
				getSwingRenderer().getReflectionUI(), parentModifStack, childModifStack, childModifAccepted,
				childValueReturnMode, commitModif, compositeModifTarget, compositeModifTitle);
	}

	public JDialog getCreatedEditor() {
		if (delegate == null) {
			return null;
		}
		return delegate.getCreatedDialog();
	}

	public boolean wasOkPressed() {
		if (delegate == null) {
			return false;
		}
		return delegate.wasOkPressed();
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
