


package xy.reflect.ui.util;

/**
 * Simple generic filter interface.
 * 
 * @author olitank
 *
 * @param <T> The type that is filtered.
 */
public interface Filter<T> {

	public static <T> Filter<T> nullFilter() {
		return new Filter<T>() {
			@Override
			public T get(T t) {
				return t;
			}
		};
	}

	/**
	 * @param t The value.
	 * @return the filtered version of the given value.
	 */
	T get(T t);

	/**
	 * Simple generic filter chain.
	 * 
	 * @author olitank
	 *
	 * @param <T> The type that is filtered.
	 */
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((filter1 == null) ? 0 : filter1.hashCode());
			result = prime * result + ((filter2 == null) ? 0 : filter2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("rawtypes")
			Chain other = (Chain) obj;
			if (filter1 == null) {
				if (other.filter1 != null)
					return false;
			} else if (!filter1.equals(other.filter1))
				return false;
			if (filter2 == null) {
				if (other.filter2 != null)
					return false;
			} else if (!filter2.equals(other.filter2))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FilterChain [filter1=" + filter1 + ", filter2=" + filter2 + "]";
		}

		
		
	}

}
