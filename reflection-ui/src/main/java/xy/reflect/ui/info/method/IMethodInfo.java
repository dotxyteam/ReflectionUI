


package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented method (or constructor)
 * properties.
 * 
 * @author olitank
 *
 */
public interface IMethodInfo extends IInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	public IMethodInfo NULL_METHOD_INFO = new IMethodInfo() {

		@Override
		public String getName() {
			return "NULL_METHOD_INFO";
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public boolean isEnabled(Object object) {
			return true;
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean b) {
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getParametersValidationCustomCaption() {
			return null;
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public boolean isNullReturnValueDistinct() {
			return false;
		}

		@Override
		public boolean isReturnValueDetached() {
			return false;
		}

		@Override
		public boolean isReturnValueIgnored() {
			return false;
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.DIRECT_OR_PROXY;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return null;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		}

		@Override
		public String getNullReturnValueLabel() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	/**
	 * 
	 * @return the name of this method. An empty string is returned if this method
	 *         is a actually constructor.
	 */
	@Override
	String getName();

	/**
	 * @return the signature of this method.
	 */
	String getSignature();

	/**
	 * @return the type information of the return value of the current method.
	 */
	ITypeInfo getReturnValueType();

	/**
	 * @return the parameters of this method.
	 */
	List<IParameterInfo> getParameters();

	/**
	 * 
	 * @param object         The object offering this method or null (if the method
	 *                       is static or is a constructor).
	 * @param invocationData The parameter values.
	 * @return the result of this method execution.
	 */
	Object invoke(Object object, InvocationData invocationData);

	/**
	 * @return true if and only if the execution of this method does not affect the
	 *         object on which it is executed.
	 */
	boolean isReadOnly();

	/**
	 * @param object The object offering this method or null (if the method is
	 *               static or is a constructor).
	 * @return whether the method execution is allowed or not.
	 */
	boolean isEnabled(Object object);

	/**
	 * @return a text that should be displayed by the method control to describe the
	 *         null return value.
	 */
	String getNullReturnValueLabel();

	/**
	 * @return the category in which this method will be displayed.
	 */
	InfoCategory getCategory();

	/**
	 * @param object         The object offering this method or null (if the method
	 *                       is static or is a constructor)
	 * @param invocationData The parameter values.
	 * @return a job that can revert the next invocation of this method or null if
	 *         the method execution cannot be reverted.
	 */
	Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData);

	/**
	 * Validates the values of the parameters. An exception is thrown if the
	 * parameter values are not valid. Otherwise the values are considered as valid.
	 * 
	 * @param object         The object offering this method or null (if the method
	 *                       is static or is a constructor)
	 * @param invocationData The parameter values.
	 * @throws Exception If the parameter values are not valid.
	 */
	void validateParameters(Object object, InvocationData invocationData) throws Exception;

	/**
	 * @return the value return mode of this method. It may impact the behavior of
	 *         the control used to display the return value.
	 */
	ValueReturnMode getValueReturnMode();

	/**
	 * @return the resource location of an image displayed on the method control or
	 *         null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return true if and only if the return value of this method should be
	 *         displayed in a separate independent view, typically a stand-alone
	 *         window.
	 */
	boolean isReturnValueDetached();

	/**
	 * @return true if and only if the control displaying the return value of this
	 *         method must distinctly display the null value. This is usually needed
	 *         if a null return value has a special meaning different from
	 *         "empty/default value" for the developer.
	 */
	boolean isNullReturnValueDistinct();

	/**
	 * @return true if and only if this method return value should be ignored.
	 */
	boolean isReturnValueIgnored();

	/**
	 * @param object         The object offering this method or null (if the method
	 *                       is static or is a constructor)
	 * @param invocationData The parameter values.
	 * @return a confirmation message to be displayed just before invoking this
	 *         method so that the user will be able to cancel the execution or null
	 *         if the execution should not be confirmed.
	 */
	String getConfirmationMessage(Object object, InvocationData invocationData);

	/**
	 * @return true if and only if the method control should be filtered out from
	 *         the display.
	 */
	boolean isHidden();

	/**
	 * @return the text displayed on the validation control (typically a 'validate'
	 *         button) of the parameters settings dialog or null if the default text
	 *         should be used.
	 */
	String getParametersValidationCustomCaption();

	/**
	 * This method is called by the renderer when the visibility of this method
	 * control changes for the given object in the generated UI.
	 * 
	 * @param object  The object offering this method or null (if the method is
	 *                static or is a constructor)
	 * @param visible true when the method becomes visible, false when it becomes
	 *                invisible.
	 */
	void onControlVisibilityChange(Object object, boolean visible);

}
