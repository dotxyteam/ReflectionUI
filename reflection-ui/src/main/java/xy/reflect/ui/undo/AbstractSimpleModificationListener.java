


package xy.reflect.ui.undo;

/**
 * This class exists as convenience for creating {@link IModificationListener}
 * objects.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSimpleModificationListener implements IModificationListener {

	/**
	 * Is called when any modification stack event occurs.
	 * 
	 * @param modification The modification related to the event.
	 */
	protected abstract void handleAnyEvent(IModification modification);

	@Override
	public void afterPush(IModification modification) {
		handleAnyEvent(modification);
	}

	@Override
	public void afterUndo(IModification undoModification) {
		handleAnyEvent(undoModification);
	}

	@Override
	public void afterRedo(IModification modification) {
		handleAnyEvent(modification);
	}

	@Override
	public void afterInvalidate() {
		handleAnyEvent(null);
	}

	@Override
	public void afterClearInvalidation() {
		handleAnyEvent(null);
	}

}
