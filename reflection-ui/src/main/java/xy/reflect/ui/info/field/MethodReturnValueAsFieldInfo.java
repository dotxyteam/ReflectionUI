
package xy.reflect.ui.info.field;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual field that returns the underlying method return value.
 * 
 * @author olitank
 *
 */
public class MethodReturnValueAsFieldInfo extends AbstractInfo implements IFieldInfo {

	protected IMethodInfo method;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo objectType;

	public MethodReturnValueAsFieldInfo(ReflectionUI reflectionUI, IMethodInfo method, ITypeInfo objectType) {
		this.reflectionUI = reflectionUI;
		if (ReflectionUIUtils.requiresParameterValue(method)) {
			throw new ReflectionUIError(
					"Cannot create field from method: Parameter value(s) required: " + method.getSignature());
		}
		if (method.getReturnValueType() == null) {
			throw new ReflectionUIError(
					"Cannot create field from method: The return type is void: " + method.getSignature());
		}
		this.method = method;
		this.objectType = objectType;
	}

	@Override
	public String getName() {
		return buildMethodReturnValueFieldName(method.getSignature());
	}

	public static String buildMethodReturnValueFieldName(String baseMethodSignature) {
		return "returnValueOf-" + ReflectionUIUtils.buildNameFromMethodSignature(baseMethodSignature);
	}

	public static String buildLegacyReturnValueFieldName(String baseMethodName) {
		return baseMethodName + ".result";
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 0.0;
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return true;
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return false;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(method.getCaption(), " Result");
	}

	@Override
	public String getOnlineHelp() {
		return method.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return method.getSpecificProperties();
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(method.getReturnValueType().getSource()) {
			@Override
			public SpecificitiesIdentifier getSpecificitiesIdentifier() {
				return new SpecificitiesIdentifier(objectType.getName(), getName());
			}

			@Override
			protected String getTypeInfoProxyFactoryIdentifier() {
				return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", baseMethod="
						+ method.getSignature() + ", objectType=" + objectType.getName() + "]";
			}
		});
	}

	@Override
	public Object getValue(Object object) {
		return method.invoke(object, new InvocationData(object, method));
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
		return null;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public boolean isTransient() {
		return method.isReadOnly();
	}

	@Override
	public String getNullValueLabel() {
		return method.getNullReturnValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return method.getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public boolean isControlValueValiditionEnabled() {
		return false;
	}

	@Override
	public IValidationJob getValueAbstractFormValidationJob(Object object) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodReturnValueAsFieldInfo other = (MethodReturnValueAsFieldInfo) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodAsField [method=" + method + "]";
	}

}
