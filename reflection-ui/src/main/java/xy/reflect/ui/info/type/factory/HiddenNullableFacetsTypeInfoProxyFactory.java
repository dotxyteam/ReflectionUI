package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class HiddenNullableFacetsTypeInfoProxyFactory extends TypeInfoProxyFactory {

	protected ReflectionUI reflectionUI;

	public HiddenNullableFacetsTypeInfoProxyFactory(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public Object generateDefaultValue(ITypeInfo type) {
		Object result;
		try {
			result = ReflectionUIUtils.createDefaultInstance(type);
		} catch (Throwable t) {
			result = null;
		}
		if (result == null) {
			throw new ReflectionUIError(
					"Unable to generate a default value for the type '" + type.getName() + "'");
		}
		return result;
	}

	@Override
	protected boolean isValueNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		Object result = super.getDefaultValue(param, method, containingType);
		if (result == null) {
			if (!isValueNullable(param, method, containingType)) {
				result = generateDefaultValue(param, method);
			}
		}
		return result;
	}

	@Override
	protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
			ITypeInfo containingType) {
		InvocationData newIinvocationData = new InvocationData();
		for (IParameterInfo param : method.getParameters()) {
			Object paramValue = invocationData.getParameterValue(param);
			if (paramValue == null) {
				if (!isValueNullable(param, method, containingType)) {
					paramValue = generateDefaultValue(param, method);
				}
			}
			newIinvocationData.setParameterValue(param, paramValue);
		}
		return super.invoke(object, newIinvocationData, method, containingType);
	}

	protected Object generateDefaultValue(IParameterInfo param, IMethodInfo method) {
		return generateDefaultValue(param.getType());
	}

	@Override
	protected boolean isValueNullable(IFieldInfo field, ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
		Object result = super.getValue(object, field, containingType);
		if (result == null) {
			if (!isValueNullable(field, containingType)) {
				result = generateDefaultValue(field);
			}
		}
		return result;
	}

	protected Object generateDefaultValue(IFieldInfo field) {
		return generateDefaultValue(field.getType());
	}

}
