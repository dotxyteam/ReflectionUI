
package xy.reflect.ui.control;

import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Context that builds its identifier from the given method and its containing
 * type.
 * 
 * @author olitank
 *
 */
public class MethodContext implements IContext {

	public static final MethodContext NULL_METHOD_CONTEXT = new MethodContext(null, null) {
		@Override
		public String getIdentifier() {
			return "NULL_METHOD_CONTEXT";
		}
	};

	protected String objectTypeName;
	protected String methodSignature;

	public MethodContext(String objectTypeName, String methodSignature) {
		this.objectTypeName = objectTypeName;
		this.methodSignature = methodSignature;
	}

	@Override
	public String getIdentifier() {
		if (ReflectionUIUtils.extractMethodNameFromSignature(methodSignature).length() == 0) {
			return "ContructorContext [type=" + objectTypeName + ", signature=" + methodSignature + "]";
		} else {
			return "MethodContext [methodSignature=" + methodSignature + ", objectType=" + objectTypeName + "]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodSignature == null) ? 0 : methodSignature.hashCode());
		result = prime * result + ((objectTypeName == null) ? 0 : objectTypeName.hashCode());
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
		if (methodSignature == null) {
			if (other.methodSignature != null)
				return false;
		} else if (!methodSignature.equals(other.methodSignature))
			return false;
		if (objectTypeName == null) {
			if (other.objectTypeName != null)
				return false;
		} else if (!objectTypeName.equals(other.objectTypeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

}
