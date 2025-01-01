
package xy.reflect.ui.info.method;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Method proxy allowing to import a source method in any type. Note that the
 * source method must be static.
 * 
 * @author olitank
 *
 */
public class ImportedMethodInfo extends MethodInfoProxy {

	protected String targetMethodName;

	public ImportedMethodInfo(ITypeInfo sourceType, String sourceMethodSignature, String targetMethodName) {
		super(retrieveSourceMethod(sourceType, sourceMethodSignature));
		this.targetMethodName = targetMethodName;
	}

	protected static IMethodInfo retrieveSourceMethod(ITypeInfo sourceType, String sourceMethodSignature) {
		IMethodInfo result = ReflectionUIUtils.findMethodBySignature(sourceType.getMethods(), sourceMethodSignature);
		if (result == null) {
			throw new ReflectionUIError("'" + sourceMethodSignature + "' not found in type '" + sourceType + "'");
		}
		return result;
	}

	@Override
	public String getName() {
		if (targetMethodName != null) {
			return targetMethodName;
		} else {
			return super.getName();
		}
	}

	@Override
	public String getSignature() {
		if (targetMethodName != null) {
			return ReflectionUIUtils.buildMethodSignature(this);
		} else {
			return super.getSignature();
		}
	}

	@Override
	public String getCaption() {
		if (targetMethodName != null) {
			return ReflectionUIUtils.identifierToCaption(targetMethodName);
		} else {
			return super.getCaption();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((targetMethodName == null) ? 0 : targetMethodName.hashCode());
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
		ImportedMethodInfo other = (ImportedMethodInfo) obj;
		if (targetMethodName == null) {
			if (other.targetMethodName != null)
				return false;
		} else if (!targetMethodName.equals(other.targetMethodName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImportedMethodInfo [sourceMethod=" + base + ", targetMethodName=" + targetMethodName + "]";
	}

}
