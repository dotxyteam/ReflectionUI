package xy.reflect.ui.util;

public class ValidationErrorWrapper extends ReflectionUIError {

	private static final long serialVersionUID = 1L;

	protected String contextCaption;

	public ValidationErrorWrapper(String contextCaption, Exception validationError) {
		super(validationError);
		this.contextCaption = contextCaption;
	}

	protected String getBaseMessage() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		return ReflectionUIUtils.composeMessage(contextCaption, getCause().toString());
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
