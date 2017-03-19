package xy.reflect.ui.control.input;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultMethodControlData implements IMethodControlData {

	protected Object object;
	protected IMethodInfo method;
	protected ITypeInfo methodOwnerType;

	public DefaultMethodControlData(ITypeInfo methodOwnerType, Object object, IMethodInfo method) {
		super();
		this.methodOwnerType = methodOwnerType;
		this.object = object;
		this.method = method;
	}

	@Override
	public String getCaption() {
		return method.getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return method.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return method.getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return method.getReturnValueType();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return method.getParameters();
	}

	@Override
	public Object invoke(InvocationData invocationData) {
		return method.invoke(object, invocationData);
	}

	@Override
	public boolean isReadOnly() {
		return method.isReadOnly();
	}

	@Override
	public String getNullReturnValueLabel() {
		return method.getNullReturnValueLabel();
	}

	@Override
	public Runnable getUndoJob(InvocationData invocationData) {
		return method.getUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(InvocationData invocationData) throws Exception {
		method.validateParameters(object, invocationData);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return method.getValueReturnMode();
	}

	@Override
	public String getMethodSignature() {
		return ReflectionUIUtils.getMethodSignature(method);
	}

	@Override
	public ITypeInfo getMethodOwnerType() {
		return methodOwnerType;
	}
	
	

}
