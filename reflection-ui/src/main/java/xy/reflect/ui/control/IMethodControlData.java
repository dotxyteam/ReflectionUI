


package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface provides what method controls need to look and behave
 * properly.
 * 
 * It is intended to be a proxy of {@link IMethodInfo} that hides pieces of
 * information that are useless for method controls in order to maximize their
 * reusability. Typically it hides the method owner object.
 */
public interface IMethodControlData {

	/**
	 * @return the type information of the underlying method return value.
	 */
	ITypeInfo getReturnValueType();

	/**
	 * @return the parameters of the underlying method.
	 */
	List<IParameterInfo> getParameters();

	/**
	 * Executes the underlying method.
	 * 
	 * @param invocationData The parameter values.
	 * @return the result of the underlying method execution.
	 */
	Object invoke(InvocationData invocationData);

	/**
	 * @param invocationData The parameter values.
	 * @return a job that can revert the next invocation of the underlying method or
	 *         null if the method execution cannot be reverted.
	 */
	Runnable getNextInvocationUndoJob(InvocationData invocationData);

	/**
	 * @return true if and only if the execution of the underlying method does not
	 *         affect the object on which it is executed.
	 */
	boolean isReadOnly();

	/**
	 * @return whether the underlying method execution is allowed or not.
	 */
	boolean isEnabled();

	/**
	 * @return a text that should be displayed by the method control to describe the
	 *         null return value.
	 */
	String getNullReturnValueLabel();

	/**
	 * Validates the values of the method parameters. An exception is thrown if the
	 * parameter values are not valid. Otherwise the values are considered as valid.
	 * 
	 * @param invocationData The parameter values.
	 * @throws Exception If the parameter values are not valid.
	 */
	void validateParameters(InvocationData invocationData) throws Exception;

	/**
	 * @return the value return mode of the underlying method. It may impact the
	 *         behavior of the control used to display the return value.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the help text that the method control must displey.
	 */
	String getOnlineHelp();

	/**
	 * @return custom properties intended to be used to extend this method control
	 *         data for specific renderers.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * @return the name that the method control must display.
	 */
	String getCaption();

	/**
	 * @return the underlying method signature.
	 */
	String getMethodSignature();

	/**
	 * @return true if and only if the return value of the underlying method should
	 *         be displayed in a separate independent view, typically a stand-alone
	 *         window.
	 */
	boolean isReturnValueDetached();

	/**
	 * @return true if and only if the control displaying the return value of the
	 *         underlying method must distinctly display the null value. This is
	 *         usually needed if a null return value has a special meaning different
	 *         from "empty/default value" for the developer.
	 */
	boolean isNullReturnValueDistinct();

	/**
	 * @return the resource location of an icon image displayed on the method
	 *         control or null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return true if and only if the underlying method return value should be
	 *         ignored.
	 */
	boolean isReturnValueIgnored();

	/**
	 * @param invocationData The parameter values.
	 * @return a confirmation message to be displayed just before invoking the
	 *         underlying method so that the user will be able to cancel the
	 *         execution or null if the execution should not be confirmed.
	 */
	String getConfirmationMessage(InvocationData invocationData);

	/**
	 * @return the resource location of a background image that must be displayed on
	 *         the method control or null.
	 */
	ResourcePath getBackgroundImagePath();

	/**
	 * @return the background color of the method control or null if the default
	 *         background color should be used.
	 */
	ColorSpecification getBackgroundColor();

	/**
	 * @return the text color of the method control or null if the default text
	 *         color should be used.
	 */
	ColorSpecification getForegroundColor();

	/**
	 * @return the border color of the method control or null if the default border
	 *         should be used.
	 */
	ColorSpecification getBorderColor();

	/**
	 * @param parameterValues The parameter values.
	 * @return the underlying method invocation data object filled with the given
	 *         parameter values. Note that it should contain the default parameter
	 *         values.
	 */
	InvocationData createInvocationData(Object... parameterValues);

	/**
	 * @param invocationData The parameter values.
	 * @param contextId      An identifier used to build the contextual type of the
	 *                       returned object.
	 * @return an object from which a form (allowing to edit the given parameter
	 *         values) can be generated.
	 */
	Object createParametersObject(InvocationData invocationData, String contextId);

	/**
	 * @return the text displayed on the validation control (typically a 'validate'
	 *         button) of the parameters settings dialog or null if the default text
	 *         should be used.
	 */
	String getParametersValidationCustomCaption();

}
