/*
 * 
 */
package xy.reflect.ui.control;

import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Class used to postpone the throwing of an exception. The exceptions are
 * caught and encapsulated in instances of this class in order to be rethrown
 * later.
 * 
 * @author olitank
 *
 */
public class ErrorOccurrence {

	protected Throwable error;

	public ErrorOccurrence(Throwable error) {
		this.error = error;
	}

	public Throwable getError() {
		return error;
	}

	public static Object tryCatch(Accessor<Object> accessor) {
		try {
			return accessor.get();
		} catch (Throwable t) {
			return new ErrorOccurrence(t);
		}
	}

	public static Object rethrow(Object object) {
		if (object instanceof ErrorOccurrence) {
			Throwable error = ((ErrorOccurrence) object).getError();
			if (error instanceof RuntimeException) {
				throw (RuntimeException) error;
			}
			if (error instanceof Error) {
				throw (Error) error;
			}
			throw new CheckExceptionWrapper(error);
		}
		return object;
	}

	public static class CheckExceptionWrapper extends ReflectionUIError {

		private static final long serialVersionUID = 1L;

		public CheckExceptionWrapper(Throwable cause) {
			super(cause);
		}

	}

}