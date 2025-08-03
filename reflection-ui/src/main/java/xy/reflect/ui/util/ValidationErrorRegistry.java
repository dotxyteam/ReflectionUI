package xy.reflect.ui.util;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;

/**
 * This class is responsible for creating, providing and destroying relations
 * between objects and their validation errors.
 * 
 * This implementation simply maps the object values to their validation errors
 * by using a key equality-based size-limited cache.
 * 
 * WARNING: it actually makes sense to map potentially multiple equal objects to
 * a single validation error, but it could lead to inconsistencies: validation
 * errors may get lost if the equals(Object) method of some validated objects
 * class is not correctly implemented, or appear on valid objects if the
 * validation depends on a context that is external to the target object. In
 * such cases a custom implementation of this class should be used in order to
 * prevent these issues.
 * 
 * @author olitank
 *
 */
public class ValidationErrorRegistry {

	protected Map<Object, Exception> attributionMap = Collections.synchronizedMap(buildAttributionMap());
	protected WeakHashMap<Thread, Boolean> validationCancellationStatusByThread = new WeakHashMap<Thread, Boolean>();

	/**
	 * @param object  The validated object.
	 * @param session The current validation session object.
	 * @return an object that will be used as a key to access the validation error
	 *         of the given object from the map returned by
	 *         {@link #buildAttributionMap()}.
	 */
	public Object getValidationErrorMapKey(Object object, ValidationSession session) {
		return object;
	}

	/**
	 * @return the map used to store the relations between objects and validation
	 *         errors.
	 */
	protected Map<Object, Exception> buildAttributionMap() {
		return MiscUtils.newStandardCache();
	}

	/**
	 * Stores/replaces the relation between the given validated object and its
	 * validation error.
	 * 
	 * @param object          The validated object.
	 * @param validationError The validation error.
	 * @param session         The current validation session object.
	 */
	public void attribute(Object object, Exception validationError, ValidationSession session) {
		attributionMap.put(getValidationErrorMapKey(object, session), validationError);
	}

	/**
	 * Removes the relation between the given validated object and an eventual
	 * validation error if it exists.
	 * 
	 * @param object
	 * @param session
	 */
	public void cancelAttribution(Object object, ValidationSession session) {
		Object attributionKey = getValidationErrorMapKey(object, session);
		if (!attributionMap.containsKey(attributionKey)) {
			return;
		}
		attributionMap.remove(attributionKey);
	}

	/**
	 * @param object  The validated object.
	 * @param session
	 * @return the validation error attributed to the given validated object if it
	 *         exists or null.
	 */
	public Exception getValidationError(Object object, ValidationSession session) {
		return attributionMap.get(getValidationErrorMapKey(object, null));
	}

	/**
	 * @param object        The object that will receive the attribution or be
	 *                      discharged from it.
	 * @param validationJob The validation job.
	 * @return a proxy of the given validation job that will attribute the eventual
	 *         validation error to the object passed as argument. Note that the
	 *         validation cancellation status is taken into account and may lead to
	 *         skipping the error attribution update.
	 */
	public IValidationJob attributing(Object object, IValidationJob validationJob) {
		return (sessionArg) -> {
			try {
				validationJob.validate(sessionArg);
				if (!isValidationCancelled(Thread.currentThread())) {
					cancelAttribution(object, sessionArg);
				}
			} catch (Throwable t) {
				if (isValidationCancelled(Thread.currentThread())) {
					return;
				}
				if (t instanceof Exception) {
					attribute(object, (Exception) t, sessionArg);
					throw (Exception) t;
				}
				throw new Error(t);
			}
		};
	}

	/**
	 * @param runnable The validation job.
	 * @return a new task that can be used to execute and cancel properly a
	 *         validation job.
	 */
	public BetterFutureTask<Boolean> createValidationTask(Runnable runnable) {
		return new BetterFutureTask<Boolean>(runnable, true) {

			@Override
			public void run() {
				setValidationCancelled(Thread.currentThread(), false);
				super.run();
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				setValidationCancelled(thread, true);
				return super.cancel(mayInterruptIfRunning);
			}

		};
	}

	/**
	 * @param thread The validation thread.
	 * @return whether the validation run by the given thread is cancelled or not.
	 */
	public boolean isValidationCancelled(Thread thread) {
		return Boolean.TRUE.equals(validationCancellationStatusByThread.get(thread));
	}

	/**
	 * Updates whether the validation run by the given thread is cancelled or not.
	 * 
	 * @param thread    The validation thread.
	 * @param cancelled The new validation cancellation status.
	 */
	public void setValidationCancelled(Thread thread, boolean cancelled) {
		if (cancelled) {
			validationCancellationStatusByThread.put(thread, Boolean.TRUE);
		} else {
			validationCancellationStatusByThread.remove(thread);
		}
	}

}
