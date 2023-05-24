
package xy.reflect.ui.util;

/**
 * Simple generic getter/setter abstract class.
 * 
 * @author olitank
 *
 * @param <T> The type that is accessed.
 */
public abstract class Accessor<T> {

	/**
	 * @return the value.
	 */
	public abstract T get();

	/**
	 * Updates the value (not supported by default).
	 * 
	 * @param t The new value.
	 */
	public void set(T t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param t The value.
	 * @return an instance returning the specified value.
	 */
	public static <T> Accessor<T> returning(final T t) {
		return returning(t, true);
	}

	/**
	 * @param t      The value.
	 * @param canSet WHether the value can be updated or not.
	 * @return an instance returning and potentially allowing to update the
	 *         specified value.
	 */
	public static <T> Accessor<T> returning(T t, boolean canSet) {
		return new BasicAccessor<T>(t, canSet);
	}

	protected static class BasicAccessor<TT> extends Accessor<TT> {

		TT value;
		boolean canSet;

		public BasicAccessor(TT value, boolean canSet) {
			this.value = value;
			this.canSet = canSet;
		}

		@Override
		public TT get() {
			return value;
		}

		@Override
		public void set(TT t) {
			if (canSet) {
				value = t;
			} else {
				throw new UnsupportedOperationException();
			}
		}

	};
}
