


package xy.reflect.ui.util;

/**
 * Main exception class of the library.
 * 
 * @author olitank
 *
 */
public class ReflectionUIError extends RuntimeException {
	protected static final long serialVersionUID = 1L;

	public ReflectionUIError() {
		super("ReflectionUI Internal Error. Check the logs for more information (the debug mode may need be activated by setting this system property: -D"
				+ SystemProperties.DEBUG + "=true)");
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
			result = "Error";
		}
		if (getCause() != null) {
			String causeClassName = getCause().getClass().getName();
			if (result.contains(causeClassName)) {
				String causeClassCaption;
				if (NullPointerException.class.getName().equals(causeClassName)) {
					causeClassCaption = "Missing Value Error";
				} else {
					causeClassCaption = ReflectionUIUtils.identifierToCaption(getCause().getClass().getSimpleName());
				}
				if (!causeClassCaption.contains("Error") && causeClassCaption.contains("Exception")) {
					causeClassCaption = causeClassCaption.replace("Exception", "Error");
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
