


package xy.reflect.ui.info.menu;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class represents a menu item that will be used to execute a given
 * method.
 * 
 * @author olitank
 *
 */
public class MethodActionMenuItemInfo extends AbstractActionMenuItemInfo {

	protected IMethodInfo method;

	public MethodActionMenuItemInfo(ReflectionUI reflectionUI, IMethodInfo method) {
		super(ReflectionUIUtils.formatMethodControlCaption(method.getCaption(), method.getParameters()),
				method.getIconImagePath());
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
		MethodActionMenuItemInfo other = (MethodActionMenuItemInfo) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodActionMenuItem [name=" + caption + ", action=" + method + "]";
	}

}
