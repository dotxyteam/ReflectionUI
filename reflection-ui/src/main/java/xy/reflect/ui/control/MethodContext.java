


package xy.reflect.ui.control;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Context that builds its identifier from the given method and its containing
 * type.
 * 
 * @author olitank
 *
 */
public class MethodContext implements IContext {

	protected ITypeInfo containingType;
	protected IMethodInfo method;

	public MethodContext(ITypeInfo containingType, IMethodInfo method) {
		this.containingType = containingType;
		this.method = method;
	}

	@Override
	public String getIdentifier() {
		if (method.getName().length() == 0) {
			return "ContructorContext [type=" + containingType.getName() + ", signature=" + method.getSignature() + "]";
		} else {
			return "MethodContext [methodSignature=" + method.getSignature() + ", containingType="
					+ containingType.getName() + "]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodContext other = (MethodContext) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodContext [containingType=" + containingType + ", method=" + method + "]";
	}

}
