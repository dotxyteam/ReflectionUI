


package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Filter that delegates to another filter that can be replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingInfoFilter implements IInfoFilter {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IInfoFilter getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

	public boolean excludeField(IFieldInfo field) {
		return getDelegate().excludeField(field);
	}

	public boolean excludeMethod(IMethodInfo method) {
		return getDelegate().excludeMethod(method);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDelegateId() == null) ? 0 : getDelegateId().hashCode());
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
		DelegatingInfoFilter other = (DelegatingInfoFilter) obj;
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingInfoFilter [delegate=" + getDelegateId() + "]";
	}

}
