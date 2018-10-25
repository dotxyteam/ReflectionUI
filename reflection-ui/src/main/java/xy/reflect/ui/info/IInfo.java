package xy.reflect.ui.info;

import java.util.Map;

/**
 * This is the base class for all the abstract UI model elements.
 * 
 * @author olitank
 *
 */
public interface IInfo {

	/**
	 * @return the name of this abstract UI model element.
	 */
	String getName();

	/**
	 * @return the displayed name of this abstract UI model element.
	 */
	String getCaption();

	/**
	 * @return the help text of this abstract UI model element.
	 */
	String getOnlineHelp();

	/**
	 * @return custom properties intended to be used to extend the abstract UI model
	 *         for specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

}
