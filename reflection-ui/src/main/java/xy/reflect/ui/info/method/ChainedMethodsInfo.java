package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.undo.IrreversibleModificationException;
import xy.reflect.ui.util.FututreActionBuilder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ChainedMethodsInfo implements IMethodInfo {

	protected IMethodInfo method1;
	protected IMethodInfo method2;
	protected FututreActionBuilder undoJobBuilder = new FututreActionBuilder();

	public ChainedMethodsInfo(IMethodInfo method1, IMethodInfo method2) {
		if (method2.getParameters().size() != 1) {
			throw new ReflectionUIError("Failed to chain methods '" + method1.getSignature() + "' AND '"
					+ method2.getSignature() + "': The 2nd method must have 1 and only 1 parameter");
		}
		this.method1 = method1;
		this.method2 = method2;
	}

	@Override
	public String getName() {
		return method1.getName() + "+" + method2.getName();
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(method1.getCaption(), method2.getCaption());
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return method2.getReturnValueType();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return method1.getParameters();
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {

		Runnable method1UndoJob = method1.getNextInvocationUndoJob(object, invocationData);
		Object result = method1.invoke(object, invocationData);

		invocationData = new InvocationData(result);

		Runnable method2UndoJob = method2.getNextInvocationUndoJob(object, invocationData);
		result = method2.invoke(object, invocationData);

		undoJobBuilder.setOption("method1UndoJob", method1UndoJob);
		undoJobBuilder.setOption("method2UndoJob", method2UndoJob);
		undoJobBuilder.build();

		return result;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return undoJobBuilder.will(new FututreActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				Runnable method1UndoJob = (Runnable) options.get("method1UndoJob");
				Runnable method2UndoJob = (Runnable) options.get("method2UndoJob");
				if (method1UndoJob == null) {
					throw new IrreversibleModificationException();
				}
				if (method2UndoJob == null) {
					throw new IrreversibleModificationException();
				}
				method2UndoJob.run();
				method1UndoJob.run();
			}
		});
	}

	@Override
	public boolean isReadOnly() {
		return method1.isReadOnly() && method2.isReadOnly();
	}

	@Override
	public String getNullReturnValueLabel() {
		return method2.getNullReturnValueLabel();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		method1.validateParameters(object, invocationData);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(method1.getValueReturnMode(), method2.getValueReturnMode());
	}

	@Override
	public ResourcePath getIconImagePath() {
		return method1.getIconImagePath();
	}

	@Override
	public IInfoProxyFactory getReturnValueTypeSpecificities() {
		return null;
	}

	@Override
	public boolean isReturnValueDetached() {
		return method1.isReturnValueDetached() || method2.isReturnValueDetached();
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return method2.isNullReturnValueDistinct();
	}

	@Override
	public boolean isReturnValueIgnored() {
		return method2.isReturnValueIgnored();
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return method1.getConfirmationMessage(object, invocationData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method1 == null) ? 0 : method1.hashCode());
		result = prime * result + ((method2 == null) ? 0 : method2.hashCode());
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
		ChainedMethodsInfo other = (ChainedMethodsInfo) obj;
		if (method1 == null) {
			if (other.method1 != null)
				return false;
		} else if (!method1.equals(other.method1))
			return false;
		if (method2 == null) {
			if (other.method2 != null)
				return false;
		} else if (!method2.equals(other.method2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChainedMethodsInfo [method1=" + method1 + ", method2=" + method2 + "]";
	}

}
