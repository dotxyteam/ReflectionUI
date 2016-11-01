package xy.reflect.ui.util;

public abstract class Accessor<T> {
	public abstract T get();

	public void set(T t) {
		throw new UnsupportedOperationException();
	}
	
	public static <T> Accessor<T> returning(final T t) {
		return returning(t, true);
	}
		

	public static <T> Accessor<T> returning(final T t, final boolean unsupportedSetter) {
		return new Accessor<T>() {
			@Override
			public T get() {
				return t;
			}

			@Override
			public void set(T t) {
				if (unsupportedSetter) {
					super.set(t);
				}
			}

		};
	}
}