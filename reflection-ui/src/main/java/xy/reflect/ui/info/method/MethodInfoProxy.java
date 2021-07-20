


package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Method information proxy class. The methods in this class should be overriden
 * to provide custom information.
 * 
 * Note that if the name, the return type or the parameters are modified then
 * the method signature should be adjusted to remain consistent.
 * 
 * @author olitank
 *
 */
public class MethodInfoProxy extends AbstractInfoProxy implements IMethodInfo {

	protected IMethodInfo base;

	public MethodInfoProxy(IMethodInfo base) {
		this.base = base;
	}

	public IMethodInfo getBase() {
		return base;
	}

	@Override
	public String getSignature() {
		return base.getSignature();
	}

	public boolean isHidden() {
		return base.isHidden();
	}

	public boolean isEnabled(Object object) {
		return base.isEnabled(object);
	}

	public void onControlVisibilityChange(Object object, boolean visible) {
		base.onControlVisibilityChange(object, visible);
	}

	public boolean isNullReturnValueDistinct() {
		return base.isNullReturnValueDistinct();
	}

	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	public boolean isReturnValueIgnored() {
		return base.isReturnValueIgnored();
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(Object object, InvocationData invocationData) {
		return base.invoke(object, invocationData);
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return base.getNextInvocationUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		base.validateParameters(object, invocationData);
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return base.getConfirmationMessage(object, invocationData);
	}

	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		MethodInfoProxy other = (MethodInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodInfoProxy [signature=" + getSignature() + ", base=" + base + "]";
	}

}
