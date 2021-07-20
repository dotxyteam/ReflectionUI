


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
import xy.reflect.ui.info.type.factory.InvocationDataObjectFactory;

/**
 * Base class of common implementations of {@link IMethodControlData}.
 * 
 * @author olitank
 *
 */
public abstract class AbstractMethodControlData implements IMethodControlData {

	protected ReflectionUI reflectionUI;

	/**
	 * @return the underlying object.
	 */
	protected abstract Object getObject();

	/**
	 * @return the underlying method information.
	 */
	protected abstract IMethodInfo getMethod();

	public AbstractMethodControlData(ReflectionUI reflectionUI) {
		super();
		this.reflectionUI = reflectionUI;
	}

	@Override
	public InvocationData createInvocationData(Object... parameterValues) {
		return new InvocationData(getObject(), getMethod(), parameterValues);
	}

	@Override
	public Object createParametersObject(InvocationData invocationData, String contextId) {
		InvocationDataObjectFactory factory = new InvocationDataObjectFactory(reflectionUI, getMethod(), contextId);
		return factory.getInstance(getObject(), invocationData);
	}

	@Override
	public ResourcePath getBackgroundImagePath() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundImagePath() != null) {
				return type.getFormButtonBackgroundImagePath();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath();
	}

	@Override
	public ColorSpecification getBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundColor() != null) {
				return type.getFormButtonBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundColor();
	}

	@Override
	public ColorSpecification getForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonForegroundColor() != null) {
				return type.getFormButtonForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonForegroundColor();
	}

	@Override
	public ColorSpecification getBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBorderColor() != null) {
				return type.getFormButtonBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBorderColor();
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return getMethod().getParametersValidationCustomCaption();
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
	public boolean isEnabled() {
		return getMethod().isEnabled(getObject());
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
	public Runnable getNextInvocationUndoJob(InvocationData invocationData) {
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
		AbstractMethodControlData other = (AbstractMethodControlData) obj;
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
		return "tMethodControlData [object=" + getObject() + ", method=" + getMethod() + "]";
	}

}
