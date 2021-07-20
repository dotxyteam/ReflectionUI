/*
 * 
 */
package xy.reflect.ui.control;

import xy.reflect.ui.util.ReflectionUIError;

/**
 * 
 * Exception class used to provide controls with a value to display along with
 * the error message.
 * 
 * @author olitank
 *
 */
public class ErrorWithDefaultValue extends ReflectionUIError {

	private static final long serialVersionUID = 1L;

	protected Throwable error;
	protected Object defaultValue;

	public ErrorWithDefaultValue(Throwable error, Object defaultValue) {
		super(error);
		this.error = error;
		this.defaultValue = defaultValue;
	}

	public Throwable getError() {
		return error;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

}