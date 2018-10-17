package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class DefaultMethodControlData implements IMethodControlData {

	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IMethodInfo method;

	public DefaultMethodControlData(ReflectionUI reflectionUI, Object object, IMethodInfo method) {
		super();
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.method = method;
	}

	public Object getObject() {
		return object;
	}

	public IMethodInfo getMethod() {
		return method;
	}

	@Override
	public ResourcePath getBackgroundImagePath() {
		return reflectionUI.getApplicationInfo().getButtonBackgroundImagePath();
	}

	@Override
	public ColorSpecification getBackgroundColor() {
		return reflectionUI.getApplicationInfo().getButtonBackgroundColor();
	}

	@Override
	public ColorSpecification getForegroundColor() {
		return reflectionUI.getApplicationInfo().getButtonForegroundColor();
	}

	@Override
	public ColorSpecification getBorderColor() {
		return reflectionUI.getApplicationInfo().getButtonBorderColor();
	}

	@Override
	public String getConfirmationMessage(InvocationData invocationData) {
		return getMethod().getConfirmationMessage(getObject(), invocationData);
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return getMethod().isNullReturnValueDistinct();
	}

	@Override
	public boolean isReturnValueDetached() {
		return getMethod().isReturnValueDetached();
	}

	public boolean isReturnValueIgnored() {
		return getMethod().isReturnValueIgnored();
	}

	@Override
	public String getCaption() {
		return getMethod().getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return getMethod().getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return getMethod().getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return getMethod().getReturnValueType();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return getMethod().getParameters();
	}

	@Override
	public Object invoke(InvocationData invocationData) {
		return getMethod().invoke(getObject(), invocationData);
	}

	@Override
	public boolean isReadOnly() {
		return getMethod().isReadOnly();
	}

	@Override
	public String getNullReturnValueLabel() {
		return getMethod().getNullReturnValueLabel();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(InvocationData invocationData) {
		return getMethod().getNextInvocationUndoJob(getObject(), invocationData);
	}

	@Override
	public void validateParameters(InvocationData invocationData) throws Exception {
		getMethod().validateParameters(getObject(), invocationData);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return getMethod().getValueReturnMode();
	}

	@Override
	public String getMethodSignature() {
		return getMethod().getSignature();
	}

	public ResourcePath getIconImagePath() {
		return getMethod().getIconImagePath();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
		result = prime * result + ((getObject() == null) ? 0 : getObject().hashCode());
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
		DefaultMethodControlData other = (DefaultMethodControlData) obj;
		if (getMethod() == null) {
			if (other.getMethod() != null)
				return false;
		} else if (!getMethod().equals(other.getMethod()))
			return false;
		if (getObject() == null) {
			if (other.getObject() != null)
				return false;
		} else if (!getObject().equals(other.getObject()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMethodControlData [object=" + getObject() + ", method=" + getMethod() + "]";
	}

}
