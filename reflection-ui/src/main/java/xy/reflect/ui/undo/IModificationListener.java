


package xy.reflect.ui.undo;

/**
 * {@link ModificationStack} listener interface.
 * 
 * @author olitank
 *
 */
public interface IModificationListener {

	/**
	 * Called after the execution of
	 * {@link ModificationStack#push(IModification)}.
	 * 
	 * @param undoModification The parameter passed to
	 *                         {@link ModificationStack#push(IModification)}.
	 */
	void afterPush(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#undo()}.
	 * 
	 * @param undoModification The undo modification that was executed.
	 */
	void afterUndo(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#redo()}.
	 * 
	 * @param modification The redo modification that was executed.
	 */
	void afterRedo(IModification modification);

	/**
	 * Called after the execution of {@link ModificationStack#invalidate()}.
	 */
	void afterInvalidate();

	/**
	 * Called after {@link ModificationStack#isInvalidated()} return value changes
	 * from true to false.
	 */
	void afterClearInvalidation();
}
