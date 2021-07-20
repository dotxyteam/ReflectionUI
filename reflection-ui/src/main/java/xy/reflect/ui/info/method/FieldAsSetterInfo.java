
package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual method that just updates the underlying field value.
 * 
 * @author olitank
 *
 */
public class FieldAsSetterInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected IFieldInfo field;
	protected IParameterInfo parameter;

	public FieldAsSetterInfo(ReflectionUI reflectionUI, IFieldInfo field, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.field = field;
		this.containingType = containingType;
		this.parameter = createParameter();
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public String getName() {
		return field.getName() + ".set";
	}

	@Override
	public String getCaption() {
		return "Set " + field.getCaption();
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isEnabled(Object object) {
		return true;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.<IParameterInfo>singletonList(parameter);
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter.getPosition());
		field.setValue(object, value);
		return null;
	}

	@Override
	public Runnable getNextInvocationUndoJob(final Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter.getPosition());
		return ReflectionUIUtils.getNextUpdateUndoJob(object, field, value);
	}

	@Override
	public boolean isReadOnly() {
		return field.isTransient();
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return null;
	}

	protected IParameterInfo createParameter() {
		return new ParameterInfoProxy(IParameterInfo.NULL_PARAMETER_INFO) {

			ITypeInfo type;

			@Override
			public String getName() {
				return field.getName();
			}

			@Override
			public ITypeInfo getType() {
				if (type == null) {
					type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(field.getType().getSource()) {
						@Override
						public SpecificitiesIdentifier getSpecificitiesIdentifier() {
							return null;
						}

						@Override
						protected String getTypeInfoProxyFactoryIdentifier() {
							return "MethodReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + ", containingType="
									+ containingType.getName() + ", baseField="
									+ field.getName() + "]";
						}
					});
				}
				return type;
			}

			@Override
			public boolean isNullValueDistinct() {
				return field.isNullValueDistinct();
			}

			@Override
			public int getPosition() {
				return 0;
			}

			@Override
			public Object getDefaultValue(Object object) {
				return field.getValue(object);
			}

			@Override
			public String toString() {
				return "Parameter [of=" + FieldAsSetterInfo.this.toString() + "]";
			}

		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldAsSetterInfo other = (FieldAsSetterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsSetter [field=" + field + "]";
	}

}
