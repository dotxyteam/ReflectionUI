package xy.reflect.ui.info;

/**
 * This interface allows to specify a way to backup and restore object states.
 * Note that there is no guarantee that each call to {@link #begin()} will be
 * followed by {@link #commit()} or {@link #rollback()}.
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
	 * Complete the last transaction started with {@link #begin()}.
	 */
	void commit();

	/**
	 * Roll back the last transaction started with {@link #begin()}.
	 */
	void rollback();

}
