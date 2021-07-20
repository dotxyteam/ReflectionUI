


package xy.reflect.ui.util;

/**
 * Simple generic listener interface.
 * 
 * @author olitank
 *
 * @param <E> The event type.
 */
public interface Listener<E> {

	/**
	 * Called when an event occurs.
	 * 
	 * @param event The current event.
	 */
	public void handle(E event);
}
