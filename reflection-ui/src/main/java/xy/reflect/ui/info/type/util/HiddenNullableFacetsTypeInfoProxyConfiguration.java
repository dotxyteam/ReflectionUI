package xy.reflect.ui.info.type.util;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class HiddenNullableFacetsTypeInfoProxyConfiguration extends
		TypeInfoProxyConfiguration {

	protected ReflectionUI reflectionUI;

	public HiddenNullableFacetsTypeInfoProxyConfiguration(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		Object result = param.getDefaultValue();
		if (result != null) {
			return result;
		}
		return getDefaultValue(param.getType());
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		Object result = super.getValue(object, field, containingType);
		if (result == null) {
			result = getDefaultValue(field.getType());
			if (!field.isReadOnly()) {
				setValue(object, result, field, containingType);
			}
		}
		return result;
	}

	@Override
	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return false;
	}

	public Object getDefaultValue(ITypeInfo type) {
		try {
			Object result = reflectionUI.onTypeInstanciationRequest(null, type,
					true);
			if (result == null) {
				throw new ReflectionUIError("Instanciation cancelled");
			}
			return result;
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Failed to automatically instanciate the type '" + type
							+ "': " + t.toString());
		}
	}
}
