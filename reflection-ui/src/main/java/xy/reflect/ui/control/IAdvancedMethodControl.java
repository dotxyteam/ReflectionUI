
package xy.reflect.ui.control;

import xy.reflect.ui.info.ValidationSession;

/**
 * Method controls implementing this interface will have more control over their
 * integration in the generated forms.
 * 
 * @author olitank
 *
 */
public interface IAdvancedMethodControl {

	/**
	 * Validates the control data. Typically, forms that are directly accessible
	 * from the current control would be validated. Note that this method is not
	 * intended to be called from the UI thread.
	 * 
	 * @param session The current validation session object.
	 * 
	 * @throws Exception If an invalid sub-form is detected.
	 */
	void validateControlData(ValidationSession session) throws Exception;

}
