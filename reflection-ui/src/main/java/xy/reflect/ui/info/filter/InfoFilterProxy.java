
package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Filter proxy class. The methods in this class should be overridden to provide
 * a custom behavior.
 * 
 * @author olitank
 *
 */
public class InfoFilterProxy implements IInfoFilter {

	protected IInfoFilter base;

	public InfoFilterProxy(IInfoFilter base) {
		this.base = base;
	}

	public IFieldInfo apply(IFieldInfo field) {
		return base.apply(field);
	}

	public IMethodInfo apply(IMethodInfo method) {
		return base.apply(method);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InfoFilterProxy [base=" + base + "]";
	}

}
