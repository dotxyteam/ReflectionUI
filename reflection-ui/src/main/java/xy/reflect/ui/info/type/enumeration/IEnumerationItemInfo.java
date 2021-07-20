


package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;

/**
 * This interface allows to specify UI-oriented properties of enumeration item
 * values.
 * 
 * @author olitank
 *
 */
public interface IEnumerationItemInfo extends IInfo {

	/**
	 * @return the location of an image resource associated with current enumeration
	 *         item or null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return the actual item value.
	 */
	Object getValue();

}
