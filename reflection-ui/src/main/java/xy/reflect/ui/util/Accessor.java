package xy.reflect.ui.util;

public abstract class Accessor<T> {
	public abstract T get();

	public void set(T t) {
		throw new UnsupportedOperationException();
	}
	
	public static <T> Accessor<T> returning(final T t) {
		return returning(t, true);
	}
		

	public static <T> Accessor<T> returning(final T t, final boolean canSet) {
		return new Accessor<T>() {
			
			T value = t;
			
			@Override
			public T get() {
				return value;
			}

			@Override
			public void set(T t) {
				if (canSet) {
					value = t;
				}else{
					throw new UnsupportedOperationException();
				}
			}

		};
	}
}