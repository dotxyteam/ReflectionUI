
package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual field that allows to view/edit the underlying method parameter value.
 * 
 * @author olitank
 *
 */
public class ParameterAsFieldInfo extends VirtualFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IParameterInfo param;
	protected IMethodInfo method;
	protected ITypeInfo containingType;

	protected ITypeInfo type;

	public ParameterAsFieldInfo(ReflectionUI reflectionUI, IMethodInfo method, IParameterInfo param,
			ITypeInfo containingType) {
		super(param.getName(), param.getType());
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
		this.method = method;
		this.param = param;
	}

	public static String buildParameterFieldName(String baseMethodSignature, String parameterName) {
		return parameterName + "Of-" + ReflectionUIUtils.buildNameFromMethodSignature(baseMethodSignature);
	}

	public static String buildLegacyParameterFieldName(String baseMethodName, String parameterName) {
		return baseMethodName + "." + parameterName;
	}

	@Override
	public String getName() {
		return buildParameterFieldName(method.getSignature(), param.getName());
	}

	@Override
	public String getCaption() {
		return param.getCaption();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(param.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(containingType.getName(), getName());
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", baseParameter="
							+ param.getName() + ", method=" + method.getSignature() + ", containingType="
							+ containingType.getName() + "]";
				}
			});
		}
		return type;
	}

	public void ensureInitialValueIsDefaultParameterValue(Object object) {
		if (!isInitialized(object)) {
			getValueByField(object).put(this, param.getDefaultValue(object));
		}
	}

	public boolean isInitialized(Object object) {
		return getValueByField(object).containsKey(this);
	}

	@Override
	public Object getValue(Object object) {
		ensureInitialValueIsDefaultParameterValue(object);
		return super.getValue(object);
	}

	@Override
	public boolean isNullValueDistinct() {
		return param.isNullValueDistinct();
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return param.hasValueOptions(object);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return param.getValueOptions(object);
	}

	@Override
	public String getOnlineHelp() {
		return param.getOnlineHelp();
	}

	@Override
	public String toString() {
		return "ParameterAsFieldInfo [method=" + method + ", param=" + param + "]";
	}

}
