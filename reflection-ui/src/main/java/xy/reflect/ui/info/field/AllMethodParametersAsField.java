package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.factory.MethodInvocationDataAsObjectFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class AllMethodParametersAsField implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String fieldName;

	protected MethodInvocationDataAsObjectFactory factory;
	protected static Map<Object, Map<IMethodInfo, InvocationData>> invocationDataByMethodByObject = new MapMaker()
			.weakKeys().makeMap();

	public AllMethodParametersAsField(ReflectionUI reflectionUI, IMethodInfo method, String fieldName) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.fieldName = fieldName;

		this.factory = createFactory();
	}

	protected MethodInvocationDataAsObjectFactory createFactory() {
		return new MethodInvocationDataAsObjectFactory(reflectionUI, method,
				"MethodParametersAsFieldContext [methodSignature=" + method.getSignature() + ", fieldName=" + fieldName
						+ "]");
	}

	@Override
	public String getName() {
		return fieldName;
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
		return reflectionUI.getTypeInfo(factory.getInstanceTypeInfoSource());
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
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
			invocationData = new InvocationData();
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
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isValueNullable() {
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
		return true;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return true;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
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
		AllMethodParametersAsField other = (AllMethodParametersAsField) obj;
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
