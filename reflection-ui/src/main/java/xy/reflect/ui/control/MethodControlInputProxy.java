


package xy.reflect.ui.control;

import xy.reflect.ui.undo.ModificationStack;

/**
 * Method control input proxy class. The methods in this class should be overriden to provide
 * a custom behavior.
 * 
 * @author olitank
 *
 */
public class MethodControlInputProxy implements IMethodControlInput {

	protected IMethodControlInput base;

	public MethodControlInputProxy(IMethodControlInput base) {
		super();
		this.base = base;
	}

	public IMethodControlData getControlData() {
		return base.getControlData();
	}

	public ModificationStack getModificationStack() {
		return base.getModificationStack();
	}

	public IContext getContext() {
		return base.getContext();
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
		MethodControlInputProxy other = (MethodControlInputProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodControlInputProxy [base=" + base + "]";
	}

}
