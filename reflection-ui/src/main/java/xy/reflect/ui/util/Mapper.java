package xy.reflect.ui.util;

public interface Mapper<T> {

	T map(T t);

	public static class Chain<T> implements Mapper<T> {

		protected Mapper<T> mapper1;
		protected Mapper<T> mapper2;

		public Chain(Mapper<T> mapper1, Mapper<T> mapper2) {
			this.mapper1 = mapper1;
			this.mapper2 = mapper2;
		}

		@Override
		public T map(T t) {
			T t1 = mapper1.map(t);
			T t2 = mapper2.map(t1);
			return t2;
		}

	}

}
