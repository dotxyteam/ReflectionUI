


package xy.reflect.ui.undo;

/**
 * Specifies how to order the execution of undo modifications according to the
 * source do/redo modifications order.
 * 
 * @author olitank
 *
 */
public enum UndoOrder {
	/**
	 * See {@link #getNormal()}.
	 */
	LIFO,

	/**
	 * See {@link #getAbnormal()}.
	 */
	FIFO;

	/**
	 * @return the {@link #LIFO} enum constant specifying that undo modifications
	 *         order should be the opposite of the do/redo modifications order.
	 */
	public static UndoOrder getNormal() {
		return LIFO;
	}

	/**
	 * @return the {@link #FIFO} enum constant specifying that undo modifications
	 *         order should be the same as the do/redo modifications order.
	 */
	public static UndoOrder getAbnormal() {
		return FIFO;
	}
};
