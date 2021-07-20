


package xy.reflect.ui.control;

import xy.reflect.ui.undo.ModificationStack;

/**
 * This interface provides contextual information and tools needed by a method
 * control to properly operate.
 * 
 * @author olitank
 *
 */
public interface IMethodControlInput {

	/**
	 * @return an object providing what method controls need to look and behave
	 *         properly.
	 */
	IMethodControlData getControlData();

	/**
	 * @return a reference to the current modification stack.
	 */
	ModificationStack getModificationStack();

	/**
	 * @return an object identifying the context in which the method will be
	 *         invoked.
	 */
	IContext getContext();
}
