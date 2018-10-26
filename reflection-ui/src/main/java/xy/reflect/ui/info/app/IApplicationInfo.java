package xy.reflect.ui.info.app;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;

/**
 * This interface allows to specify a set of common abstract UI properties.
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
	 * @return the custom text color of generated labels/controls or null.
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
	 * @return the custom background color of generated buttons or null.
	 */
	ColorSpecification getButtonBackgroundColor();

	/**
	 * @return the custom text color of generated buttons or null.
	 */
	ColorSpecification getButtonForegroundColor();

	/**
	 * @return the custom border color of generated buttons or null.
	 */
	ColorSpecification getButtonBorderColor();

	/**
	 * @return the resource location of a background image displayed on generated
	 *         buttons or null.
	 */
	ResourcePath getButtonBackgroundImagePath();

	/**
	 * @return whether the generated windows use the cross-platform system
	 *         integration (title bar) or the native one.
	 */
	boolean isSystemIntegrationCrossPlatform();

	/**
	 * @return the custom background color of the title bar of generated windows or
	 *         null. Note that it is taken into account only if
	 *         {@link #isSystemIntegrationCrossPlatform()} returns true.
	 */
	ColorSpecification getTitleBackgroundColor();

	/**
	 * @return the custom text color of the title bar of generated windows or null.
	 *         Note that it is taken into account only if
	 *         {@link #isSystemIntegrationCrossPlatform()} returns true.
	 */
	ColorSpecification getTitleForegroundColor();

	/**
	 * @return the resource location of a background image used as the system icon
	 *         of the generated windows or null.
	 */
	ResourcePath getIconImagePath();

}
