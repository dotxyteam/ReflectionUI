package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.method.IMethodInfo;

public class MethodActionMenuItem extends AbstractActionMenuItem {

	private static final long serialVersionUID = 1L;

	protected IMethodInfo method;

	public MethodActionMenuItem(IMethodInfo method) {
		super(method.getCaption(), method.getIconImagePath());
		this.method = method;
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public void setMethod(IMethodInfo method) {
		this.method = method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodActionMenuItem other = (MethodActionMenuItem) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActionMenuItem [name=" + name + ", action=" + method + "]";
	}

}
