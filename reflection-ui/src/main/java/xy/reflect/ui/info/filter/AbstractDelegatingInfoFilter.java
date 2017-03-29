package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public abstract class AbstractDelegatingInfoFilter implements IInfoFilter {

	protected abstract IInfoFilter getDelegate();

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
		result = prime * result + ((getDelegate() == null) ? 0 : getDelegate().hashCode());
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
		AbstractDelegatingInfoFilter other = (AbstractDelegatingInfoFilter) obj;
		if (getDelegate() == null) {
			if (other.getDelegate() != null)
				return false;
		} else if (!getDelegate().equals(other.getDelegate()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getDelegate().toString();
	}

}
