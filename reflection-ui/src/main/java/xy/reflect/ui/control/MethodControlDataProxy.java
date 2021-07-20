


package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Method control data proxy class. The methods in this class should be overriden to provide a
 * custom behavior.
 * 
 * @author olitank
 *
 */
public class MethodControlDataProxy implements IMethodControlData {

	protected IMethodControlData base;

	public MethodControlDataProxy(IMethodControlData base) {
		super();
		this.base = base;
	}

	public InvocationData createInvocationData(Object... parameterValues) {
		return base.createInvocationData(parameterValues);
	}

	public Object createParametersObject(InvocationData invocationData, String contextId) {
		return base.createParametersObject(invocationData, contextId);
	}

	public boolean isEnabled() {
		return base.isEnabled();
	}

	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	public ResourcePath getBackgroundImagePath() {
		return base.getBackgroundImagePath();
	}

	public ColorSpecification getBackgroundColor() {
		return base.getBackgroundColor();
	}

	public ColorSpecification getForegroundColor() {
		return base.getForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return base.getBorderColor();
	}

	@Override
	public String getConfirmationMessage(InvocationData invocationData) {
		return base.getConfirmationMessage(invocationData);
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

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(InvocationData invocationData) {
		return base.invoke(invocationData);
	}

	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	public Runnable getNextInvocationUndoJob(InvocationData invocationData) {
		return base.getNextInvocationUndoJob(invocationData);
	}

	public void validateParameters(InvocationData invocationData) throws Exception {
		base.validateParameters(invocationData);
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getMethodSignature() {
		return base.getMethodSignature();
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
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
		MethodControlDataProxy other = (MethodControlDataProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodControlDataProxy [base=" + base + "]";
	}

}
