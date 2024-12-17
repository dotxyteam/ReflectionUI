


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

	protected ITypeInfo objectType;
	protected IMethodInfo method;

	public MethodContext(ITypeInfo objectType, IMethodInfo method) {
		this.objectType = objectType;
		this.method = method;
	}

	@Override
	public String getIdentifier() {
		if (method.getName().length() == 0) {
			return "ContructorContext [type=" + objectType.getName() + ", signature=" + method.getSignature() + "]";
		} else {
			return "MethodContext [methodSignature=" + method.getSignature() + ", objectType="
					+ objectType.getName() + "]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
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
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
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
		return "MethodContext [objectType=" + objectType + ", method=" + method + "]";
	}

}
