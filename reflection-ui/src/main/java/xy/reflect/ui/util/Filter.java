package xy.reflect.ui.util;

public interface Filter<T> {

	T get(T t);

	public static class Chain<T> implements Filter<T> {

		protected Filter<T> filter1;
		protected Filter<T> filter2;

		public Chain(Filter<T> filter1, Filter<T> filter2) {
			this.filter1 = filter1;
			this.filter2 = filter2;
		}

		@Override
		public T get(T t) {
			T t1 = filter1.get(t);
			T t2 = filter2.get(t1);
			return t2;
		}

	}

}
