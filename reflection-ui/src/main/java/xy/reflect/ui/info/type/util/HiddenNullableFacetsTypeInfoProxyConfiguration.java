package xy.reflect.ui.info.type.util;

import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class HiddenNullableFacetsTypeInfoProxyConfiguration extends
		TypeInfoProxyConfiguration {

	private static final Object NULL_FOR_CACHE = new Object();
	protected ReflectionUI reflectionUI;
	protected Map<ITypeInfo, Object> defaultValueByType = CacheBuilder
			.newBuilder().maximumSize(100).<ITypeInfo, Object> build().asMap();

	public HiddenNullableFacetsTypeInfoProxyConfiguration(
			ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		Object result = param.getDefaultValue();
		if (result != null) {
			return result;
		}
		return generateDefaultValue(param.getType());
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return (param.getDefaultValue() == null)
				&& (generateDefaultValue(param.getType()) == null);
	}

	@Override
	protected Object getValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		Object result = super.getValue(object, field, containingType);
		if (result == null) {
			result = generateDefaultValue(field.getType());
		}
		return result;
	}

	@Override
	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return generateDefaultValue(field.getType()) == null;
	}

	public Object generateDefaultValue(ITypeInfo type) {
		Object result = defaultValueByType.get(type);
		if (result == null) {
			try {
				result = reflectionUI.onTypeInstanciationRequest(null, type,
						true);
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
}
