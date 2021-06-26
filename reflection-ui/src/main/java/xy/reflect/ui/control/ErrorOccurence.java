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
public class ErrorOccurence extends ReflectionUIError {

	private static final long serialVersionUID = 1L;

	public ErrorOccurence(Throwable error) {
		super(error);
	}

	public static Object tryCatch(Accessor<Object> accessor) {
		try {
			return accessor.get();
		} catch (Throwable t) {
			if (t instanceof ErrorOccurence) {
				return t;
			}
			return new ErrorOccurence(t);
		}
	}

	public static Object rethrow(Object object) {
		if (object instanceof ErrorOccurence) {
			throw (ErrorOccurence) object;
		}
		return object;
	}

}