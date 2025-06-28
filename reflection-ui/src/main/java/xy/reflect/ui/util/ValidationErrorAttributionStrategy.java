package xy.reflect.ui.util;

import java.util.Map;

import xy.reflect.ui.info.ValidationSession;

/**
 * This class is responsible for creating, providing and destroying relations
 * between objects and validation errors. This implementation simply maps the
 * object values to their validation errors by using a key equality-based
 * size-limited cache.
 * 
 * WARNING: it actually makes sense to map multiple equal objects to a single
 * validation error, but it could lead to inconsistencies: validation errors may
 * get lost when the equals(Object) method is not correctly implemented, or
 * appear on components of valid object when the validation depends on a
 * context. In such cases a custom implementation should be used in order to
 * prevent these issues.
 * 
 * @author olitank
 *
 */
public class ValidationErrorAttributionStrategy {

	/**
	 * @param object  The validated object.
	 * @param session The current validation session object.
	 * @return an object that will be used as a key to access the validation error
	 *         of the given object from the map returned by
	 *         {@link #buildAttributionMap()}.
	 */
	protected Object getValidationErrorMapKey(Object object, ValidationSession session) {
		return object;
	}

	/**
	 * @return the map used to store the relations between objects and validation
	 *         errors.
	 */
	public Map<Object, Exception> buildAttributionMap() {
		return MiscUtils.newStandardCache();
	}

	/**
	 * Stores/replaces the relation between the given validated object and its
	 * validation error.
	 * 
	 * @param validationErrorMap The map used to store the relations between objects
	 *                           and validation errors.
	 * @param session            The current validation session object.
	 * @param object             The validated object.
	 * @param validationError    The validation error.
	 */
	public void attribute(Map<Object, Exception> validationErrorMap, ValidationSession session, Object object,
			Exception validationError) {
		validationErrorMap.put(getValidationErrorMapKey(object, session), validationError);
	}

	/**
	 * Removes the relation between the given validated object and an eventual
	 * validation error if it exists.
	 * 
	 * @param validationErrorMap
	 * @param session
	 * @param object
	 */
	public void cancelAttribution(Map<Object, Exception> validationErrorMap, ValidationSession session, Object object) {
		validationErrorMap.remove(getValidationErrorMapKey(object, session));
	}

	/**
	 * @param validationErrorMap The map used to store the relations between objects
	 *                           and validation errors.
	 * @param object             The validated object.
	 * @return the validation error attributed to the given validated object if it
	 *         exists or null.
	 */
	public Exception getValidationError(Map<Object, Exception> validationErrorMap, Object object) {
		return validationErrorMap.get(getValidationErrorMapKey(object, null));
	}

}
