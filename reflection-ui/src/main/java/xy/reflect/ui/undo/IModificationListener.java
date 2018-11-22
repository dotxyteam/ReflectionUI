package xy.reflect.ui.undo;

/**
 * {@link ModificationStack} listener interface.
 * 
 * @author nikolat
 *
 */
public interface IModificationListener {

	/**
	 * Called after the execution of
	 * {@link ModificationStack#pushUndo(IModification)}.
	 * 
	 * @param undoModification
	 */
	void handlePush(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#undo()}.
	 * 
	 * @param undoModification
	 */
	void handleUdno(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#redo()}.
	 * 
	 * @param modification
	 */
	void handleRedo(IModification modification);

	/**
	 * Called after the execution of {@link ModificationStack#invalidate()} or
	 * {@link ModificationStack#forget()}.
	 */
	void handleInvalidate();

	/**
	 * Called after {@link ModificationStack#isInvalidated()} return value changes
	 * from true to false. {@link ModificationStack#forget()}.
	 */
	void handleInvalidationCleared();
}
