
package xy.reflect.ui.info.method;

import java.util.ArrayList;
import java.util.List;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Method proxy that allows to provide a specific parameter value and execute
 * the base method without exposing this parameter and requiring its value.
 * 
 * @author olitank
 *
 */
public class ProvidedParameterValueMethodInfoProxy extends MethodInfoProxy {

	protected String newMethodName;
	protected int providedParameterPosition;
	protected Object providedParameterValue;

	public ProvidedParameterValueMethodInfoProxy(IMethodInfo base, String newMethodName, int providedParameterPosition,
			Object providedParameterValue) {
		super(base);
		this.newMethodName = newMethodName;
		this.providedParameterPosition = providedParameterPosition;
		this.providedParameterValue = providedParameterValue;
	}

	@Override
	public String getName() {
		return newMethodName;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public String getCaption() {
		String result = super.getCaption();
		if ((super.getParameters().size() > 0) && (getParameters().size() == 0) && result.endsWith("...")) {
			result = result.substring(0, result.length() - "...".length());
		}
		return result;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = super.getParameters();
		result = new ArrayList<IParameterInfo>(result);
		if (!result.removeIf(parameter -> parameter.getPosition() == providedParameterPosition)) {
			throw new ReflectionUIError();
		}
		return result;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return super.invoke(object, buildFinalInvocationData(invocationData));
	}

	protected InvocationData buildFinalInvocationData(InvocationData invocationData) {
		InvocationData result = new InvocationData(invocationData);
		result.getProvidedParameterValues().put(providedParameterPosition, providedParameterValue);
		return result;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return super.getNextInvocationUndoJob(object, buildFinalInvocationData(invocationData));
	}

	@Override
	public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
		return super.getPreviousInvocationCustomRedoJob(object, buildFinalInvocationData(invocationData));
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((newMethodName == null) ? 0 : newMethodName.hashCode());
		result = prime * result + providedParameterPosition;
		result = prime * result + ((providedParameterValue == null) ? 0 : providedParameterValue.hashCode());
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
		ProvidedParameterValueMethodInfoProxy other = (ProvidedParameterValueMethodInfoProxy) obj;
		if (newMethodName == null) {
			if (other.newMethodName != null)
				return false;
		} else if (!newMethodName.equals(other.newMethodName))
			return false;
		if (providedParameterPosition != other.providedParameterPosition)
			return false;
		if (providedParameterValue == null) {
			if (other.providedParameterValue != null)
				return false;
		} else if (!providedParameterValue.equals(other.providedParameterValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProvidedParameterValueMethodInfoProxy [newMethodName=" + newMethodName + ", providedParameterPosition="
				+ providedParameterPosition + ", providedParameterValue=" + providedParameterValue + "]";
	}

}
