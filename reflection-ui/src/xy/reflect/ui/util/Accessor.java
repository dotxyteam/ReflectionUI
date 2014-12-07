package xy.reflect.ui.util;

public abstract class Accessor<T> {
	public abstract T get();

	public void set(T t) {
		throw new UnsupportedOperationException();
	}

	public static <T> Accessor<T> returning(final T t) {
		return new Accessor<T>() {
			@Override
			public T get() {
				return t;
			}
		};
	}
}