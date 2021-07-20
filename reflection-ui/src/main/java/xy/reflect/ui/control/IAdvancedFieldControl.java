


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
