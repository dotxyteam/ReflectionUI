package xy.reflect.ui.undo;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SlaveModificationStack extends ModificationStack {

	protected SwingRenderer swingRenderer;
	protected Form form;
	protected Accessor<Boolean> valueModifAcceptedGetter;
	protected Accessor<ValueReturnMode> valueReturnModeGetter;
	protected Accessor<Boolean> valueReplacedGetter;
	protected Accessor<String> editSessionTitleGetter;
	protected Accessor<ModificationStack> masterModificationStackGetter;
	protected Accessor<IModification> commitModifGetter;
	protected boolean exclusiveLinkWithParent;

	public SlaveModificationStack(SwingRenderer swingRenderer, Form form, Accessor<Boolean> valueModifAcceptedGetter,
			Accessor<ValueReturnMode> valueReturnModeGetter, Accessor<Boolean> valueReplacedGetter,
			Accessor<IModification> commitModifGetter, Accessor<String> editSessionTitleGetter,
			Accessor<ModificationStack> masterModificationStackGetter, boolean exclusiveLinkWithParent) {
		super(null);
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.valueModifAcceptedGetter = valueModifAcceptedGetter;
		this.valueReturnModeGetter = valueReturnModeGetter;
		this.valueReplacedGetter = valueReplacedGetter;
		this.commitModifGetter = commitModifGetter;
		this.editSessionTitleGetter = editSessionTitleGetter;
		this.masterModificationStackGetter = masterModificationStackGetter;
		this.exclusiveLinkWithParent = exclusiveLinkWithParent;

	}

	@Override
	public boolean pushUndo(IModification undoModif) {
		if (undoModif.isNull()) {
			return false;
		}
		boolean result = super.pushUndo(undoModif);
		if (isInComposite()) {
			return result;
		}
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.pushUndo(new ModificationStackShitf(this, -1, undoModif.getTitle()) {

			@Override
			protected void shiftBackward() {
				SlaveModificationStack.this.super_undo();
			}

			@Override
			protected void shiftForeward() {
				SlaveModificationStack.this.super_redo();
			}

		});
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		IModification commitModif = commitModifGetter.get();
		String editSessionTitle = AbstractModification.getUndoTitle(undoModif.getTitle());
		String editSessionTitlePrefix = editSessionTitleGetter.get();
		if (editSessionTitlePrefix != null) {
			editSessionTitle = ReflectionUIUtils.composeMessage(editSessionTitlePrefix, editSessionTitle);
		}
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		return ReflectionUIUtils.finalizeSeparateObjectValueEditSession(parentObjectModifStack, valueModifStack,
				valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTitle,
				ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		forwardInvalidation();
	}

	protected void forwardInvalidation() {
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.invalidate();
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		IModification commitModif = commitModifGetter.get();
		String editSessionTitle = null;
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		ReflectionUIUtils.finalizeSeparateObjectValueEditSession(parentObjectModifStack, valueModifStack,
				valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTitle,
				ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
	}

	@Override
	public void forget() {
		super.forget();
		if (exclusiveLinkWithParent) {
			ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
			parentObjectModifStack.forget();
		} else {
			forwardInvalidation();
		}
	}

	@Override
	public void undo() {
		super_undo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	@Override
	public void redo() {
		super_redo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	protected void super_undo() {
		super.undo();
	}

	protected void super_redo() {
		super.redo();
	}

	@Override
	public String toString() {
		return SlaveModificationStack.class.getSimpleName() + "[of " + form.toString() + ", to "
				+ masterModificationStackGetter.get() + "]";
	}

}
