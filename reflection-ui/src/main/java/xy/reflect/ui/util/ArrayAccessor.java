


package xy.reflect.ui.util;

import java.util.Arrays;

/**
 * Simple generic getter/setter class that stores the accessed value in an
 * array.
 * 
 * @author olitank
 *
 * @param <T> The type that is accessed.
 */
public class ArrayAccessor<T> extends Accessor<T> {

	protected T[] arrayValue;

	public ArrayAccessor(T[] arrayValue) {
		super();
		this.arrayValue = arrayValue;
	}

	@Override
	public T get() {
		return arrayValue[0];
	}

	@Override
	public void set(T t) {
		arrayValue[0] = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arrayValue);
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
		ArrayAccessor<?> other = (ArrayAccessor<?>) obj;
		if (!Arrays.equals(arrayValue, other.arrayValue))
			return false;
		return true;
	}

}
