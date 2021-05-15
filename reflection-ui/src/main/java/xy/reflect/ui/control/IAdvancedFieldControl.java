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
 * Field control implementing this interface will gain more control on their
 * integration in the generated forms.
 * 
 * @author olitank
 *
 */
public interface IAdvancedFieldControl {

	/**
	 * @return whether the current control successfully displayed its caption. If
	 *         false is returned then the framework should take care of displaying
	 *         the control caption.
	 */
	boolean showsCaption();

	/**
	 * Updates the state of the current control.
	 * 
	 * @param refreshStructure Whether the current control should update its
	 *                         structure to reflect the recent meta-data change.
	 *                         Mainly used in design mode.
	 * @return whether the current control successfully updated its state. If false
	 *         is returned then the renderer will replace the current control by
	 *         another one able to display the current value.
	 */
	boolean refreshUI(boolean refreshStructure);

	/**
	 * @throws Exception When an invalid sub-form is detected.
	 */
	void validateSubForm() throws Exception;

	/**
	 * Allows controls to forward menu contributions of sub-controls (forms mainly).
	 * 
	 * @param menuModel The menu model to be fed.
	 */
	void addMenuContribution(MenuModel menuModel);

	/**
	 * @return whether the current control requested successfully the focus. If
	 *         false is returned then the framework should adjust the focus
	 *         accordingly.
	 */
	boolean requestCustomFocus();

	/**
	 * @return whether the following features should be handled by the framework (if
	 *         false is returned then the current control should take care of them):
	 *         busy indication, undo management, error display.
	 */
	boolean isAutoManaged();

	/**
	 * @param msg The error message to be displayed.
	 * @return whether the given error message was successfully displayed by the
	 *         current control. If false is returned then the framework should take
	 *         care of displaying the error message. Note that the framework will
	 *         take this into account if and only if {@link #isAutoManaged()}
	 *         returns false.
	 */
	boolean displayError(String msg);

}
