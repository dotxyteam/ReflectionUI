package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public class VirtualFieldInfo extends AbstractInfo implements IFieldInfo {

	protected String fieldName;
	protected ITypeInfo fieldType;

	protected static Map<Object, Map<IFieldInfo, Object>> valueByFieldByObject = new MapMaker().weakKeys().makeMap();

	public VirtualFieldInfo(String fieldName, ITypeInfo fieldType) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
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
		return fieldType;
	}

	@Override
	public IInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		Map<IFieldInfo, Object> valueByField = getValueByField(object);
		return valueByField.get(this);
	}

	protected Map<IFieldInfo, Object> getValueByField(Object object) {
		Map<IFieldInfo, Object> valueByField = valueByFieldByObject.get(object);
		if (valueByField == null) {
			valueByField = new HashMap<IFieldInfo, Object>();
			valueByFieldByObject.put(object, valueByField);
		}
		return valueByField;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		getValueByField(object).put(this, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object newValue) {
		return null;
	}

	@Override
	public boolean isNullValueDistinct() {
		return !getType().isPrimitive();
	}

	@Override
	public boolean isGetOnly() {
		return false;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
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
		VirtualFieldInfo other = (VirtualFieldInfo) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!fieldType.equals(other.fieldType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VirtualFieldInfo [fieldName=" + fieldName + ", fieldType=" + fieldType + "]";
	}

}
