


package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify UI-oriented properties of enumeration types.
 * Note that values are distinct from item information instances that actually
 * enrich the values.
 * 
 * @author olitank
 *
 */
public interface IEnumerationTypeInfo extends ITypeInfo {

	/**
	 * @return the list of enumerated items. Note that there may be other values
	 *         (not in this list) that are supported by this type
	 *         ({@link #supports(Object)} returns true).
	 */
	Object[] getValues();

	/**
	 * @param value A possible value of this type.
	 * @return the enumeration item information associated with the given value.
	 */
	IEnumerationItemInfo getValueInfo(Object value);

	/**
	 * @return true if and only if the possible values of this type are subject to
	 *         change. A false return value would typically allow the renderer to
	 *         perform optimizations.
	 */
	boolean isDynamicEnumeration();
}
