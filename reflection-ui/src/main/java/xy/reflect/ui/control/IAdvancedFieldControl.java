/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.control;

import xy.reflect.ui.info.menu.MenuModel;

/**
 * Field controls implementing this interface will have more control over their
 * integration in the generated forms.
 * 
 * @author olitank
 *
 */
public interface IAdvancedFieldControl {

	/**
	 * Instructs the current control to display the field caption.
	 * 
	 * @return whether the current control successfully displayed the field caption.
	 *         If false is returned then the renderer will take care of the field
	 *         caption display.
	 */
	boolean showsCaption();

	/**
	 * Updates the values displayed by the current control.
	 * 
	 * @param refreshStructure Whether the current control should update its
	 *                         structure to reflect the recent meta-data changes.
	 *                         Mainly used in design mode.
	 * @return whether the current control successfully updated its state. If false
	 *         is returned then the renderer will replace the current control by
	 *         another one able to display the current value.
	 */
	boolean refreshUI(boolean refreshStructure);

	/**
	 * Validates forms that are embedded in the current control.
	 * 
	 * @throws Exception If an invalid sub-form is detected.
	 */
	void validateSubForms() throws Exception;

	/**
	 * Allows the current control to contribute to its generated window menu.
	 * 
	 * @param menuModel The menu model to be fed.
	 */
	void addMenuContributions(MenuModel menuModel);

	/**
	 * Requests that the current control get the input focus
	 * 
	 * @return whether the current control focus request was successful or not.
	 */
	boolean requestCustomFocus();

	/**
	 * @return whether the following features are handled by the current control
	 *         itself (if false is returned then the renderer will take care of
	 *         them): undo management, error display.
	 */
	boolean isAutoManaged();

	/**
	 * Instructs the current control to display the specified error message.
	 * 
	 * @param msg The error message to be displayed.
	 * @return whether the given error message was displayed by the current control
	 *         or not. If false is returned then the renderer will take care of the
	 *         error message display. Note that the renderer will not call this
	 *         method if {@link #isAutoManaged()} returns true.
	 */
	boolean displayError(String msg);

}
