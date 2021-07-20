


package xy.reflect.ui.info;

import java.util.Arrays;
import java.util.Collections;

/**
 * This enumeration allows to specify what kind of relation exists between a
 * value returned by a class member (field or a method) and its source object.
 * 
 * @author olitank
 *
 */
public enum ValueReturnMode {

	/**
	 * Means that the value is a reference or a proxy of a value stored in the
	 * source object. Thus altering the value will alter the source object.
	 */
	DIRECT_OR_PROXY,

	/**
	 * Means that the value is not stored in the source object. It is either a copy
	 * or a calculation result. Thus altering the value will not alter the source
	 * object.
	 */
	CALCULATED,

	/**
	 * Means that the value could be stored in the source object or not. Thus
	 * altering the value may alter (or not) the source object.
	 */
	INDETERMINATE;

	/**
	 * 
	 * @param parent The parent return mode.
	 * @param child  The child return mode.
	 * @return the result of the combination of 2 value return modes as if a parent
	 *         value was obtained using the parent value return mode and a child
	 *         value was obtained from the parent value using the child return value
	 *         mode.
	 */
	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		return Collections.max(Arrays.asList(parent, child));
	}
}
