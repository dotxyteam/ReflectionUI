


package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Filter proxy class. The methods in this class should be overriden to provide a custom behavior.
 * 
 * @author olitank
 *
 */
public class InfoFilterProxy implements IInfoFilter {

	IInfoFilter delegate;

	public InfoFilterProxy(IInfoFilter delegate) {
		super();
		this.delegate = delegate;
	}

	public boolean excludeField(IFieldInfo field) {
		return delegate.excludeField(field);
	}

	public boolean excludeMethod(IMethodInfo method) {
		return delegate.excludeMethod(method);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		InfoFilterProxy other = (InfoFilterProxy) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InfoFilterProxy [base=" + delegate + "]";
	}

}
