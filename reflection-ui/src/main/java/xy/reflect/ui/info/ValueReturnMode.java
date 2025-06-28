
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
	 * Means that the value is an sub-object stored in the source object. Thus
	 * altering the value is equivalent to altering the source object.
	 */
	DIRECT,

	/**
	 * (for backward compatibility) {@link #DIRECT} or {@link #PROXY}.
	 */
	@Deprecated
	DIRECT_OR_PROXY,

	/**
	 * Means that the value is not a sub-object stored in the source object,
	 * nevertheless altering the value will result in altering the source object.
	 */
	PROXY,

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

	/**
	 * @param valueReturnMode an item of this enumeration.
	 * @return whether the given valueReturnMode is {@link #DIRECT}, {@link #PROXY},
	 *         or {@value #DIRECT_OR_PROXY}.
	 */
	public static boolean isDirectOrProxy(ValueReturnMode valueReturnMode) {
		if (valueReturnMode == DIRECT) {
			return true;
		}
		if (valueReturnMode == PROXY) {
			return true;
		}
		if (valueReturnMode == DIRECT_OR_PROXY) {
			return true;
		}
		return false;
	}
}
