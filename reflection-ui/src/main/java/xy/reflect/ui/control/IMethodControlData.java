
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
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;

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
	 * Behaves like {@link IMethodInfo#getReturnValueType()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	ITypeInfo getReturnValueType();

	/**
	 * Behaves like {@link IMethodInfo#getParameters()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	List<IParameterInfo> getParameters();

	/**
	 * Behaves like {@link IMethodInfo#invoke(Object, InvocationData)} with a
	 * specific underlying object.
	 * 
	 * @param invocationData Similar to the parameter of
	 *                       {@link IMethodInfo#invoke(Object, InvocationData)}.
	 * @return the value corresponding to the behavior described above.
	 */
	Object invoke(InvocationData invocationData);

	/**
	 * Behaves like
	 * {@link IMethodInfo#getNextInvocationUndoJob(Object, InvocationData)} with a
	 * specific underlying object.
	 * 
	 * @param invocationData Similar to the parameter of
	 *                       {@link IMethodInfo#getNextInvocationUndoJob(Object, InvocationData)}.
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getNextInvocationUndoJob(InvocationData invocationData);

	/**
	 * Behaves like
	 * {@link IMethodInfo#getPreviousInvocationCustomRedoJob(Object, InvocationData)}
	 * with a specific underlying object.
	 * 
	 * @param invocationData Similar to the parameter of
	 *                       {@link IMethodInfo#getPreviousInvocationCustomRedoJob(Object, InvocationData)}.
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getPreviousInvocationCustomRedoJob(InvocationData invocationData);

	/**
	 * Behaves like {@link IMethodInfo#isReadOnly()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isReadOnly();

	/**
	 * Behaves like {@link IMethodInfo#isEnabled()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isEnabled();

	/**
	 * Behaves like {@link IMethodInfo#getNullReturnValueLabel()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getNullReturnValueLabel();

	/**
	 * Behaves like {@link IMethodInfo#validateParameters(Object, InvocationData)}
	 * with a specific underlying object.
	 * 
	 * @param invocationData Similar to the parameter of
	 *                       {@link IMethodInfo#validateParameters(Object, InvocationData)}.
	 * @throws Exception An exception corresponding to the behavior described above.
	 */
	void validateParameters(InvocationData invocationData) throws Exception;

	/**
	 * Behaves like {@link IMethodInfo#getValueReturnMode()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * Behaves like {@link IMethodInfo#getOnlineHelp()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getOnlineHelp();

	/**
	 * Behaves like {@link IMethodInfo#getSpecificProperties()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	Map<String, Object> getSpecificProperties();

	/**
	 * Behaves like {@link IMethodInfo#getCaption()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getCaption();

	/**
	 * Behaves like {@link IMethodInfo#getSignature()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getSignature();

	/**
	 * Behaves like {@link IMethodInfo#isReturnValueDetached()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isReturnValueDetached();

	/**
	 * Behaves like {@link IMethodInfo#isNullReturnValueDistinct()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isNullReturnValueDistinct();

	/**
	 * Behaves like {@link IMethodInfo#getIconImagePath()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	ResourcePath getIconImagePath();

	/**
	 * Behaves like {@link IMethodInfo#isReturnValueIgnored()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isReturnValueIgnored();

	/**
	 * Behaves like
	 * {@link IMethodInfo#getConfirmationMessage(Object, InvocationData)} with a
	 * specific underlying object.
	 * 
	 * @param invocationData Similar to the parameter of
	 *                       {@link IMethodInfo#getConfirmationMessage(Object, InvocationData)}.
	 * @return the value corresponding to the behavior described above.
	 */
	String getConfirmationMessage(InvocationData invocationData);

	/**
	 * Behaves like {@link IMethodInfo#getParametersValidationCustomCaption()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getParametersValidationCustomCaption();

	/**
	 * Behaves like {@link IMethodInfo#getExecutionSuccessMessage()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	String getExecutionSuccessMessage();

	/**
	 * Behaves like {@link ITypeInfo#getLastFormRefreshStateRestorationJob(Object)}
	 * with a specific underlying object.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	Runnable getLastFormRefreshStateRestorationJob();

	/**
	 * Behaves like {@link IMethodInfo#isControlReturnValueValiditionEnabled()}.
	 * 
	 * @return the value corresponding to the behavior described above.
	 */
	boolean isControlReturnValueValiditionEnabled();

	/**
	 * Behaves like
	 * {@link IMethodInfo#getReturnValueAbstractFormValidationJob(Object, Object)}
	 * with a specific underlying object.
	 * 
	 * @param returnValue Similar to the parameter of
	 *                    {@link IMethodInfo#getReturnValueAbstractFormValidationJob(Object, Object)}.
	 * @return the value corresponding to the behavior described above.
	 * 
	 */
	IValidationJob getReturnValueAbstractFormValidationJob(Object returnValue);

	/**
	 * @return the resource location of a background image that should be displayed
	 *         on the method control or null.
	 */
	ResourcePath getBackgroundImagePath();

	/**
	 * @return the resource location of the font object that should be used to
	 *         display text on the method control or null.
	 */
	ResourcePath getCustomFontResourcePath();

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

}
