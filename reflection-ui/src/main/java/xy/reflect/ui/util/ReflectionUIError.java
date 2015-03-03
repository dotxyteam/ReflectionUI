package xy.reflect.ui.util;

public class ReflectionUIError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ReflectionUIError() {
		super("ReflectionUI Internal Error");
	}

	public ReflectionUIError(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionUIError(String message) {
		super(message);
	}

	public ReflectionUIError(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		String result = super.getMessage();
		if (result == null) {
			result = ReflectionUIUtils.getPrintedStackTrace(this);
		}
		if (getCause() != null) {
			String causeClassName = getCause().getClass().getName();
			if (result.contains(causeClassName)) {
				String causeClassCaption;
				if (NullPointerException.class.getName().equals(causeClassName)) {
					causeClassCaption= "Missing Value Error";
				} else {
					causeClassCaption = ReflectionUIUtils
							.identifierToCaption(getCause().getClass()
									.getSimpleName());
				}
				if (!causeClassCaption.contains("Error")
						&& causeClassCaption.contains("Exception")) {
					causeClassCaption = causeClassCaption.replace("Exception",
							"Error");
				}
				result = result.replace(causeClassName, causeClassCaption);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
