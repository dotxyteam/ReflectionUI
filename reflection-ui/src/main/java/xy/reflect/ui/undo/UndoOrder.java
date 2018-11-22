package xy.reflect.ui.undo;

/**
 * Specifies how to order the execution of undo modifications according to the
 * source do/redo modifications order.
 * 
 * @author nikolat
 *
 */
public enum UndoOrder {
	/**
	 * @see #getNormal().
	 */
	FIFO,

	/**
	 * @see #getInverse().
	 */
	LIFO;

	/**
	 * @return the {@link #LIFO} enum constant specifying that undo modifications
	 *         order should be the same as the source do/redo modifications order.
	 */
	public static UndoOrder getNormal() {
		return LIFO;
	}

	/**
	 * @return the {@link #FIFO} enum constant specifying that undo modifications
	 *         order should be reverted according to the source do/redo
	 *         modifications order.
	 */
	public static UndoOrder getInverse() {
		return FIFO;
	}
};
