package xy.reflect.ui.undo;

public abstract class AbstractSimpleModificationListener implements IModificationListener {

	protected abstract void handleAnyEvent(IModification modification);

	@Override
	public void handlePush(IModification modification) {
		handleAnyEvent(modification);
	}

	@Override
	public void handleUdno(IModification undoModification) {
		handleAnyEvent(undoModification);
	}

	@Override
	public void handleRedo(IModification modification) {
		handleAnyEvent(modification);
	}

	@Override
	public void handleInvalidate() {
		handleAnyEvent(null);
	}

	@Override
	public void handleInvalidationCleared() {
		handleAnyEvent(null);
	}

}
