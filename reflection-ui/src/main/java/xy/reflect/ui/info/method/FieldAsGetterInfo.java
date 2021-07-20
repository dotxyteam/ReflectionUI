
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
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual method that just returns the underlying field value.
 * 
 * @author olitank
 *
 */
public class FieldAsGetterInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo field;
	protected ITypeInfo containingType;
	protected ITypeInfo returnValueType;

	public FieldAsGetterInfo(ReflectionUI reflectionUI, IFieldInfo field, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.field = field;
		this.containingType = containingType;
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
		return field.getName() + ".get";
	}

	@Override
	public String getCaption() {
		return "Show " + field.getCaption();
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
		field.onControlVisibilityChange(object, visible);
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
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return field.isNullValueDistinct();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return field.getValue(object);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return field.getValueReturnMode();
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (returnValueType == null) {
			returnValueType = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(field.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return null;
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "MethodReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + ", containingType="
							+ containingType.getName() + ", baseField=" + field.getName() + "]";
				}
			});
		}
		return returnValueType;

	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public String getNullReturnValueLabel() {
		return field.getNullValueLabel();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
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
		FieldAsGetterInfo other = (FieldAsGetterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsGetter [field=" + field + "]";
	}

}
