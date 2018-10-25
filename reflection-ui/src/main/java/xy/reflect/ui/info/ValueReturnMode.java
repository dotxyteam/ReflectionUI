package xy.reflect.ui.info;

import java.util.Arrays;
import java.util.Collections;

/**
 * This enumeration allows to specify what kind of relation exists between a
 * value returned by a field or a method and its source object.
 * 
 * @author olitank
 *
 */
public enum ValueReturnMode {

	/**
	 * The value is a reference to a value or a proxy of a value stored in the
	 * source object. Thus altering the value will alter the source object.
	 */
	DIRECT_OR_PROXY,

	/**
	 * The value is not stored in the source object. It is either a copy or a
	 * calcilation result. Thus altering the value will not alter the source object.
	 */
	CALCULATED,

	/**
	 * The value could be stored in the source object or not. Thus altering the
	 * could alter or not the source object.
	 */
	INDETERMINATE;

	/**
	 * 
	 * @param parent
	 *            The parent return mode.
	 * @param child
	 *            The child return mode.
	 * @return the result of the combination of 2 overlaid value return modes.
	 */
	public static ValueReturnMode combine(ValueReturnMode parent, ValueReturnMode child) {
		return Collections.max(Arrays.asList(parent, child));
	}
}
