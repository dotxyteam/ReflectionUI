package xy.reflect.ui.control;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IMethodControlData {

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(InvocationData invocationData);

	boolean isReadOnly();

	String getNullReturnValueLabel();

	Runnable getNextUpdateCustomUndoJob(InvocationData invocationData);

	void validateParameters(InvocationData invocationData) throws Exception;

	ValueReturnMode getValueReturnMode();

	String getOnlineHelp();

	Map<String, Object> getSpecificProperties();

	String getCaption();

	String getMethodSignature();

	boolean isReturnValueDetached();

	boolean isNullReturnValueDistinct();
	
	ResourcePath getIconImagePath();

	boolean isReturnValueIgnored();

	String getConfirmationMessage(InvocationData invocationData);
}
