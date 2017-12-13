package xy.reflect.ui.undo;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ForwardingModificationStack extends ModificationStack {

	protected SwingRenderer swingRenderer;
	protected JPanel form;
	protected Accessor<Boolean> valueModifAcceptedGetter;
	protected Accessor<ValueReturnMode> valueReturnModeGetter;
	protected Accessor<Boolean> valueReplacedGetter;
	protected Accessor<IInfo> editSessionTargetGetter;
	protected Accessor<String> editSessionTitleGetter;
	protected Accessor<ModificationStack> parentObjectModifStackGetter;
	protected Accessor<IModification> commitModifGetter;
	private boolean exclusiveForwarding;

	public ForwardingModificationStack(SwingRenderer swingRenderer, JPanel form,
			Accessor<Boolean> valueModifAcceptedGetter, Accessor<ValueReturnMode> valueReturnModeGetter,
			Accessor<Boolean> valueReplacedGetter, Accessor<IModification> commitModifGetter,
			Accessor<IInfo> editSessionTargetGetter, Accessor<String> editSessionTitleGetter,
			Accessor<ModificationStack> parentObjectModifStackGetter, boolean exclusiveForwarding) {
		super(null);
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.valueModifAcceptedGetter = valueModifAcceptedGetter;
		this.valueReturnModeGetter = valueReturnModeGetter;
		this.valueReplacedGetter = valueReplacedGetter;
		this.commitModifGetter = commitModifGetter;
		this.editSessionTargetGetter = editSessionTargetGetter;
		this.editSessionTitleGetter = editSessionTitleGetter;
		this.parentObjectModifStackGetter = parentObjectModifStackGetter;
		this.exclusiveForwarding = exclusiveForwarding;
		swingRenderer.getModificationStackForwardingStatusByForm().put(form, true);

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
		valueModifStack.pushUndo(
				new ModificationStackShitfModification(this, -1, undoModif.getTitle(), undoModif.getTarget()) {

					@Override
					protected void shiftBackward() {
						ForwardingModificationStack.this.super_undo();
					}

					@Override
					protected void shiftForeward() {
						ForwardingModificationStack.this.super_redo();
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
		ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
		IInfo editSessionTarget = editSessionTargetGetter.get();
		return ReflectionUIUtils.finalizeSeparateObjectValueEditSession(parentObjectModifStack, valueModifStack,
				valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTarget, editSessionTitle,
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
		IInfo editSessionTarget = editSessionTargetGetter.get();
		ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
		ReflectionUIUtils.finalizeSeparateObjectValueEditSession(parentObjectModifStack, valueModifStack,
				valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTarget, editSessionTitle,
				ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
	}

	@Override
	public void forget() {
		super.forget();
		if (exclusiveForwarding) {
			ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
			parentObjectModifStack.forget();
		} else {
			forwardInvalidation();
		}
	}

	@Override
	public void undo() {
		super_undo();
		ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	@Override
	public void redo() {
		super_redo();
		ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
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
		return ForwardingModificationStack.class.getSimpleName() + "[of " + form.toString() + ", to "
				+ parentObjectModifStackGetter.get() + "]";
	}

}
