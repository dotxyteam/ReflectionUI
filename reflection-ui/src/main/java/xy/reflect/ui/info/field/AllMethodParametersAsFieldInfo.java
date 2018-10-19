package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.MethodInvocationDataAsObjectFactory;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class AllMethodParametersAsFieldInfo extends AbstractInfo implements IFieldInfo {

	protected static Map<Object, Map<IMethodInfo, InvocationData>> invocationDataByMethodByObject = new MapMaker()
			.weakKeys().makeMap();

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String fieldName;
	protected MethodInvocationDataAsObjectFactory factory;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public AllMethodParametersAsFieldInfo(ReflectionUI reflectionUI, IMethodInfo method, String fieldName,
			ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.fieldName = fieldName;
		this.containingType = containingType;

		this.factory = createFactory();
	}

	protected MethodInvocationDataAsObjectFactory createFactory() {
		String contextId = "MethodParametersAsFieldContext [methodSignature=" + method.getSignature() + ", fieldName="
				+ fieldName + "]";
		return new MethodInvocationDataAsObjectFactory(reflectionUI, method, contextId);
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(factory
					.getInstanceTypeInfoSource(new SpecificitiesIdentifier(containingType.getName(), fieldName)));
		}
		return type;
	}

	@Override
	public Object getValue(Object object) {
		Map<IMethodInfo, InvocationData> invocationDataByMethod = invocationDataByMethodByObject.get(object);
		if (invocationDataByMethod == null) {
			invocationDataByMethod = new HashMap<IMethodInfo, InvocationData>();
			invocationDataByMethodByObject.put(object, invocationDataByMethod);
		}
		InvocationData invocationData = invocationDataByMethod.get(method);
		if (invocationData == null) {
			invocationData = new InvocationData(method);
			invocationDataByMethod.put(method, invocationData);
		}
		return factory.getInstance(object, invocationData);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
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
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public InfoCategory getCategory() {
		return method.getCategory();
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		AllMethodParametersAsFieldInfo other = (AllMethodParametersAsFieldInfo) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodParametersField [method=" + method + ", fieldName=" + fieldName + "]";
	}

}
