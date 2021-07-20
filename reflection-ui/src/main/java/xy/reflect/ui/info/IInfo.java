


package xy.reflect.ui.info;

import java.util.Map;

/**
 * This is the base interface of all the abstract UI model elements.
 * 
 * @author olitank
 *
 */
public interface IInfo {

	/**
	 * @return the name of this abstract UI model element. It may be empty but not
	 *         null.
	 */
	String getName();

	/**
	 * @return the displayed name of this abstract UI model element. It may be empty
	 *         but not null.
	 */
	String getCaption();

	/**
	 * @return the help text of this abstract UI model element or null.
	 */
	String getOnlineHelp();

	/**
	 * @return custom properties intended to be used to extend the abstract UI model
	 *         for specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

}
