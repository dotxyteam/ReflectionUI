


package xy.reflect.ui.info.app;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;

/**
 * This interface allows to specify a set of global abstract UI properties.
 * 
 * @author olitank
 *
 */
public interface IApplicationInfo extends IInfo {

	/**
	 * @return the custom background color of generated windows or null.
	 */
	ColorSpecification getMainBackgroundColor();

	/**
	 * @return the custom text color of generated labels or null.
	 */
	ColorSpecification getMainForegroundColor();

	/**
	 * @return the custom border color of generated controls or null.
	 */
	ColorSpecification getMainBorderColor();

	/**
	 * @return the resource location of a background image displayed on generated
	 *         windows or null.
	 */
	ResourcePath getMainBackgroundImagePath();

	/**
	 * @return the custom text color of generated editor controls or null.
	 */
	ColorSpecification getMainEditorForegroundColor();

	/**
	 * @return the custom background color of generated editor controls or null.
	 */
	ColorSpecification getMainEditorBackgroundColor();

	/**
	 * @return the custom background color of generated buttons or null.
	 */
	ColorSpecification getMainButtonBackgroundColor();

	/**
	 * @return the custom text color of generated buttons or null.
	 */
	ColorSpecification getMainButtonForegroundColor();

	/**
	 * @return the custom border color of generated buttons or null.
	 */
	ColorSpecification getMainButtonBorderColor();

	/**
	 * @return the resource location of a background image displayed on generated
	 *         buttons or null.
	 */
	ResourcePath getMainButtonBackgroundImagePath();

	/**
	 * @return whether the generated windows use the cross-platform system
	 *         integration (title bar) or the native one.
	 */
	boolean isSystemIntegrationCrossPlatform();

	/**
	 * @return the custom title bar background color of generated windows or null.
	 *         Note that it is taken into account only if
	 *         {@link #isSystemIntegrationCrossPlatform()} returns true.
	 */
	ColorSpecification getTitleBackgroundColor();

	/**
	 * @return the custom title bar text color of generated windows or null. Note
	 *         that it is taken into account only if
	 *         {@link #isSystemIntegrationCrossPlatform()} returns true.
	 */
	ColorSpecification getTitleForegroundColor();

	/**
	 * @return the resource location of a background image used as the system icon
	 *         of the generated windows or null.
	 */
	ResourcePath getIconImagePath();

}
