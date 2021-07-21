


package xy.reflect.ui.util;

/**
 * Simple generic visitor interface.
 * 
 * @author olitank
 *
 * @param <T> The type of the visited elements.
 */
public interface Visitor<T> {

	/**
	 * Called for each visited element.
	 * 
	 * @param t The currently visited element.
	 * @return whether the visit should continue or not.
	 */
	boolean visit(T t);

}
