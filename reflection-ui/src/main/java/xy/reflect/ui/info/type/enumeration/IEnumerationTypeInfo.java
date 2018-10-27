package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify UI-oriented properties of enumeration types.
 * 
 * @author olitank
 *
 */
public interface IEnumerationTypeInfo extends ITypeInfo {

	/**
	 * @return the list of enumerated values.
	 */
	Object[] getPossibleValues();

	/**
	 * @param value
	 *            An item of the possible values of this type.
	 * @return a UI-oriented descriptor of the given item.
	 */
	IEnumerationItemInfo getValueInfo(Object value);

	/**
	 * @return true if and only if the possible values of this type are subject to
	 *         change. A false value would typically allow the renderer to perform
	 *         optimizations.
	 */
	boolean isDynamicEnumeration();
}
