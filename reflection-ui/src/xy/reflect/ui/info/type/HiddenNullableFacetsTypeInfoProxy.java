package xy.reflect.ui.info.type;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIException;

public class HiddenNullableFacetsTypeInfoProxy extends TypeInfoProxy {

	protected ReflectionUI reflectionUI;

	public HiddenNullableFacetsTypeInfoProxy(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	protected Object getDefaultValue(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return getDefaultValue(param.getType());
	}

	@Override
	protected boolean isNullable(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return false;
	}

	@Override
	protected Object getValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		Object result = super.getValue(object, field, containingType);
		;
		if (result == null) {
			result = getDefaultValue(field.getType());
			setValue(object, result, field, containingType);
		}
		return result;
	}

	@Override
	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return false;
	}

	public Object getDefaultValue(ITypeInfo type) {
		Object result = reflectionUI.onTypeInstanciationRequest(null, type,
				true, true);
		if (result == null) {
			throw new ReflectionUIException(
					"Failed to instanciate automatically the type '" + type
							+ "'");
		}
		return result;
	}

}
