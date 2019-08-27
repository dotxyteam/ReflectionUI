/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
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
