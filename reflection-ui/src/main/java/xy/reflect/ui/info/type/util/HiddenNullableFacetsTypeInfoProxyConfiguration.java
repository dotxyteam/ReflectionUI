package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class HiddenNullableFacetsTypeInfoProxyConfiguration extends TypeInfoProxyConfiguration {

	private static final Object NULL_FOR_CACHE = new Object();
	protected ReflectionUI reflectionUI;
	protected Map<ITypeInfo, Object> defaultValueByType = CacheBuilder.newBuilder().maximumSize(100)
			.<ITypeInfo, Object> build().asMap();

	public HiddenNullableFacetsTypeInfoProxyConfiguration(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		if (!param.isNullable()) {
			return false;
		}
		return (param.getDefaultValue() == null) && (generateDefaultValue(param.getType()) == null);
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		Object result = super.getDefaultValue(param, method, containingType);
		if (result == null) {
			result = generateDefaultValue(param.getType());
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
				paramValue = generateDefaultValue(param.getType());
			}
			newIinvocationData.setparameterValue(param, paramValue);
		}
		return super.invoke(object, newIinvocationData, method, containingType);
	}

	public Object generateDefaultValue(ITypeInfo type) {
		Object result = defaultValueByType.get(type);
		if (result == null) {
			try {
				result = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(null, type, true);
				if (result == null) {
					result = NULL_FOR_CACHE;
				}
			} catch (Throwable t) {
				result = NULL_FOR_CACHE;
			}
			defaultValueByType.put(type, result);
		}
		if (result == NULL_FOR_CACHE) {
			result = null;
		}
		return result;
	}

	@Override
	protected Component createFieldControl(ITypeInfo type, final Object object, final IFieldInfo field) {
		return super.createFieldControl(type, object, new FieldInfoProxy(field) {
			@Override
			public boolean isNullable() {
				return field.getValue(object) == null;
			}
		});
	}

}
