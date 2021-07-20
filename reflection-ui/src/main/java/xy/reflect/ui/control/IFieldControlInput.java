


package xy.reflect.ui.control;

import xy.reflect.ui.undo.ModificationStack;

/**
 * This interface provides contextual information and tools needed by a field
 * control to properly display and modify the field values.
 * 
 * @author olitank
 *
 */
public interface IFieldControlInput {

	/**
	 * @return an object providing what field controls need to look and behave
	 *         properly.
	 */
	IFieldControlData getControlData();

	/**
	 * @return a reference to the current modification stack.
	 */
	ModificationStack getModificationStack();

	/**
	 * @return an object identifying the context in which the field is displayed.
	 */
	IContext getContext();

}
