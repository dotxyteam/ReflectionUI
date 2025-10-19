package xy.reflect.ui.util;

/**
 * Exception class that is used to throw specific validation errors that add
 * some context information to their underlying validation error (cause). It
 * also provides information on the fact that a component that threw this kind
 * of exception is not the ultimate source of the validation error, but rather a
 * container of this source component. The framework should then be able to
 * highlight distinctly only this source component.
 * 
 * @author olitank
 *
 */
public class ValidationErrorWrapper extends ReflectionUIError {

	private static final long serialVersionUID = 1L;

	protected String contextInformation;

	public ValidationErrorWrapper(String contextInformation, Exception validationError) {
		super(validationError);
		this.contextInformation = contextInformation;
	}

	protected String getBaseMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		return ReflectionUIUtils.composeMessage(contextInformation, getBaseMessage());
	}

	public static Exception unwrapValidationError(Exception e) {
		if (e instanceof ValidationErrorWrapper) {
			if (e.getCause() instanceof Exception) {
				if (((ValidationErrorWrapper) e).getBaseMessage() != null) {
					if (((ValidationErrorWrapper) e).getBaseMessage().contains(e.getCause().toString())) {
						return unwrapValidationError((Exception) e.getCause());
					}
				}
			}
		}
		return e;
	}

}
