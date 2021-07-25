/*
 * 
 */
package xy.reflect.ui.info;

import xy.reflect.ui.undo.ModificationStack;

/**
 * This interface allows to specify a way to backup and restore object states.
 * It typically allows to cancel safely a lot of modifications (since
 * {@link ModificationStack} has size limitations). Each call to
 * {@link #begin()} will be followed by a call to {@link #commit()} or
 * {@link #rollback()}.
 * 
 * @author olitank
 *
 */
public interface ITransactionInfo {

	/**
	 * Starts a new transaction.
	 */
	void begin();

	/**
	 * Completes the last transaction started with {@link #begin()}.
	 */
	void commit();

	/**
	 * Rolls back the last transaction started with {@link #begin()}.
	 */
	void rollback();

}
