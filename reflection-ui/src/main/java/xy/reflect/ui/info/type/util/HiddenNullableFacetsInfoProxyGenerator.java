package xy.reflect.ui.info.type.util;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class HiddenNullableFacetsInfoProxyGenerator extends InfoProxyGenerator {

	protected ReflectionUI reflectionUI;

	public HiddenNullableFacetsInfoProxyGenerator(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public Object generateDefaultValue(ITypeInfo type) {
		Object result;
		try {
			result = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(null, type, true);
		} catch (Throwable t) {
			result = null;
		}
		if (result == null) {
			throw new ReflectionUIError(HiddenNullableFacetsInfoProxyGenerator.class.getSimpleName()
					+ " was unable to generate a default value for the type '" + type + "'");
		}
		return result;
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		Object result = super.getDefaultValue(param, method, containingType);
		if (result == null) {
			if (!isNullable(param, method, containingType)) {
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
				if (!isNullable(param, method, containingType)) {
					paramValue = generateDefaultValue(param, method);
				}
			}
			newIinvocationData.setparameterValue(param, paramValue);
		}
		return super.invoke(object, newIinvocationData, method, containingType);
	}

	protected Object generateDefaultValue(IParameterInfo param, IMethodInfo method) {
		try {
			return generateDefaultValue(param.getType());
		} catch (ReflectionUIError e) {
			throw new ReflectionUIError("Parameter '" + param + "' of Method '" + method + "': " + e.toString(), e);
		}
	}

	@Override
	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
		Object result = super.getValue(object, field, containingType);
		if (result == null) {
			if (!isNullable(field, containingType)) {
				result = generateDefaultValue(field);
			}
		}
		return result;
	}

	protected Object generateDefaultValue(IFieldInfo field) {
		try {
			return generateDefaultValue(field.getType());
		} catch (ReflectionUIError e) {
			throw new ReflectionUIError("Field '" + field + "': " + e.toString(), e);
		}
	}

}
