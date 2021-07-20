/*
 * 
 */
package xy.reflect.ui.undo;

/**
 * Exception thrown from {@link IModification#applyAndGetOpposite()} when it
 * appears that a modification has been aborted.
 * 
 * @author olitank
 *
 */
public class CancelledModificationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
