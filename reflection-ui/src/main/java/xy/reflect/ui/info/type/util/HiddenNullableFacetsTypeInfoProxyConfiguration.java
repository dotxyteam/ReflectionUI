package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
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
	protected String getDebugInfo() {
		return "HiddenNullableFacets";
	}

	@Override
	protected boolean isNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		if (!param.isNullable()) {
			return false;
		}
		return (param.getDefaultValue() == null) && (generateDefaultValue(param.getType()) == null);
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
	protected Component createFieldControl(ITypeInfo type, Object object, IFieldInfo field) {
		Component result = super.createFieldControl(type, object, field);
		if (result instanceof NullableControl) {
			result = new NullableControl(reflectionUI, object, field,
					((NullableControl) result).getNonNullFieldValueControlCreator()) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void setShouldBeNull(boolean b) {
					super.setShouldBeNull(b);
					nullingControl.setVisible(b);
				}

			};
		}
		return result;
	}

}
