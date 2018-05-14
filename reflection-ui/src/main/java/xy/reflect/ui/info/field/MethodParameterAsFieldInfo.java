package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;

public class MethodParameterAsFieldInfo extends AbstractInfo implements IFieldInfo {
	protected IParameterInfo param;
	protected IMethodInfo method;

	protected static Map<Object, Map<IMethodInfo, Map<IParameterInfo, Object>>> valueByParameterByMethodByObject = new MapMaker()
			.weakKeys().makeMap();

	public MethodParameterAsFieldInfo(IMethodInfo method, IParameterInfo param) {
		this.method = method;
		this.param = param;
	}

	protected Map<IParameterInfo, Object> getValueByParameter(Object object) {
		Map<IMethodInfo, Map<IParameterInfo, Object>> valueByParameterByMethod = valueByParameterByMethodByObject
				.get(object);
		if (valueByParameterByMethod == null) {
			valueByParameterByMethod = new HashMap<IMethodInfo, Map<IParameterInfo, Object>>();
			valueByParameterByMethodByObject.put(object, valueByParameterByMethod);
		}
		Map<IParameterInfo, Object> valueByParameter = valueByParameterByMethod.get(method);
		if (valueByParameter == null) {
			valueByParameter = new HashMap<IParameterInfo, Object>();
			valueByParameterByMethod.put(method, valueByParameter);
		}
		return valueByParameter;
	}

	@Override
	public Object getValue(Object object) {
		Map<IParameterInfo, Object> valueByParameter = getValueByParameter(object);
		if (!valueByParameter.containsKey(param)) {
			return param.getDefaultValue();
		}
		return valueByParameter.get(param);
	}

	@Override
	public void setValue(Object object, Object value) {
		getValueByParameter(object).put(param, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isNullValueDistinct() {
		return param.isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		return param.getType();
	}

	@Override
	public IInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public String getCaption() {
		return param.getCaption();
	}

	@Override
	public boolean isGetOnly() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public String getName() {
		return param.getName();
	}
	
	@Override
	public boolean isHidden() {
		return param.isHidden();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return param.getOnlineHelp();
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
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((param == null) ? 0 : param.hashCode());
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
		MethodParameterAsFieldInfo other = (MethodParameterAsFieldInfo) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (param == null) {
			if (other.param != null)
				return false;
		} else if (!param.equals(other.param))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodParameterAsField [method=" + method + ", param=" + param + "]";
	}

}