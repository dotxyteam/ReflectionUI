


package xy.reflect.ui.util;

/**
 * Simple generic pair class.
 * 
 * @author olitank
 *
 * @param <T1> The first element type.
 * @param <T2> The second elemnt type.
 */
public class Pair<T1, T2> {

	protected T1 first;
	protected T2 second;

	/**
	 * Main constructor.
	 * 
	 * @param first  The first element.
	 * @param second The second element.
	 */
	public Pair(T1 first, T2 second) {
		if (first == null) {
			throw new IllegalArgumentException("first == null");
		}
		if (second == null) {
			throw new IllegalArgumentException("second == null");
		}
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first element.
	 */
	public T1 getFirst() {
		return first;
	}

	/**
	 * @return the second element.
	 */
	public T2 getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair)) {
			return false;
		}
		@SuppressWarnings({ "rawtypes" })
		Pair other = (Pair) obj;
		if (!first.equals(other.first)) {
			return false;
		}
		if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "first=" + first + "\nsecond=" + second;
	}

}
