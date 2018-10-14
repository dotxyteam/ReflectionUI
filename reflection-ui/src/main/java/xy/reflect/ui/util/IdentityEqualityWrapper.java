package xy.reflect.ui.util;

public class IdentityEqualityWrapper<T> {

	private T object;

	public IdentityEqualityWrapper(T object) {
		super();
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		IdentityEqualityWrapper<?> other = (IdentityEqualityWrapper<?>) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (object != other.object)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IdentityEqualityWrapper [object=" + object + "]";
	}

}
