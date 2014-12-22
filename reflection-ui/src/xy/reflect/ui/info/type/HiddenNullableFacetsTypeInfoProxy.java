package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.HiddenNullableFacetParameterInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;

public class HiddenNullableFacetsTypeInfoProxy extends TypeInfoProxy {

	protected ReflectionUI reflectionUI;

	public HiddenNullableFacetsTypeInfoProxy(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	protected List<IFieldInfo> getTypeFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : super.getTypeFields(type)) {
			result.add(new HiddenNullableFacetFieldInfoProxy(
					reflectionUI, field));
		}
		return result;
	}

	@Override
	protected List<IParameterInfo> getMethodParameters(IMethodInfo method,
			ITypeInfo containingType) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : super.getMethodParameters(method,
				containingType)) {
			result.add(new HiddenNullableFacetParameterInfoProxy(
					reflectionUI, param));
		}
		return result;
	}

}
