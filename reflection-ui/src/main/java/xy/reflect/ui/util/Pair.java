
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pair [first=" + first + ", second=" + second + "]";
	}

}
