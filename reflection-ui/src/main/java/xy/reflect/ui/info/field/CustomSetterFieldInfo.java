package xy.reflect.ui.info.field;

import java.util.List;
import java.util.stream.Collectors;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CustomSetterFieldInfo extends FieldInfoProxy {

	protected IMethodInfo customSetter;
	protected ReflectionUI reflectionUI;
	protected String customSetterSignature;
	protected ITypeInfo objectType;

	public CustomSetterFieldInfo(ReflectionUI reflectionUI, IFieldInfo field, String customSetterSignature,
			ITypeInfo objectType) {
		super(field);
		this.reflectionUI = reflectionUI;
		this.customSetterSignature = customSetterSignature;
		this.objectType = objectType;
	}

	protected IMethodInfo getCustomSetter() {
		if (customSetter == null) {
			IMethodInfo method = ReflectionUIUtils.findMethodBySignature(objectType.getMethods(),
					customSetterSignature);
			if (method == null) {
				throw new ReflectionUIError(
						"Field '" + getName() + "': Custom setter not found: '" + customSetterSignature + "'");
			}
			IParameterInfo valueParameter = expectValueParameter(method);
			{
				Class<?> parameterValueClass;
				try {
					parameterValueClass = reflectionUI.getReflectedClass(valueParameter.getType().getName());
				} catch (Exception e) {
					parameterValueClass = null;
				}
				Class<?> fieldValueClass;
				try {
					fieldValueClass = reflectionUI.getReflectedClass(getType().getName());
				} catch (Exception e) {
					fieldValueClass = null;
				}
				if (ClassUtils.areIncompatible(fieldValueClass, parameterValueClass)) {
					throw new ReflectionUIError("Field '" + getName() + "': Invalid custom setter: '"
							+ customSetterSignature + "': Parameter type '" + parameterValueClass.getName()
							+ "' is incompatible with field type '" + fieldValueClass.getName() + "'");
				}
			}
			customSetter = method;
		}
		return customSetter;
	}

	protected IParameterInfo expectValueParameter(IMethodInfo method) {
		List<IParameterInfo> visibleParameters = method.getParameters().stream()
				.filter(parameter -> !parameter.isHidden()).collect(Collectors.toList());
		if (visibleParameters.size() != 1) {
			throw new ReflectionUIError("Field '" + getName() + "': Invalid custom setter: '" + customSetterSignature
					+ "': Unexpected number of visible parameters: "
					+ visibleParameters.stream().map(IParameterInfo::getName).collect(Collectors.toList())
					+ ": Expected 1 and only 1 parameter");
		}
		return visibleParameters.get(0);
	}

	@Override
	public boolean isGetOnly() {
		return false;
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo customSetter = getCustomSetter();
		InvocationData invocationData = new InvocationData(object, customSetter);
		IParameterInfo valueParameter = expectValueParameter(customSetter);
		invocationData.getProvidedParameterValues().put(valueParameter.getPosition(), value);
		customSetter.invoke(object, invocationData);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		IMethodInfo customSetter = getCustomSetter();
		InvocationData invocationData = new InvocationData(object, customSetter);
		IParameterInfo valueParameter = expectValueParameter(customSetter);
		invocationData.getProvidedParameterValues().put(valueParameter.getPosition(), value);
		return customSetter.getNextInvocationUndoJob(object, invocationData);
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object value) {
		IMethodInfo customSetter = getCustomSetter();
		InvocationData invocationData = new InvocationData(object, customSetter);
		IParameterInfo valueParameter = expectValueParameter(customSetter);
		invocationData.getProvidedParameterValues().put(valueParameter.getPosition(), value);
		return customSetter.getPreviousInvocationCustomRedoJob(object, invocationData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((customSetterSignature == null) ? 0 : customSetterSignature.hashCode());
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
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
		CustomSetterFieldInfo other = (CustomSetterFieldInfo) obj;
		if (customSetterSignature == null) {
			if (other.customSetterSignature != null)
				return false;
		} else if (!customSetterSignature.equals(other.customSetterSignature))
			return false;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomSetterFieldInfo [customSetterSignature=" + customSetterSignature + ", objectType=" + objectType
				+ "]";
	}
}
