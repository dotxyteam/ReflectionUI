package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldAsGetter implements IMethodInfo {

	protected IFieldInfo field;
	protected String getterName;

	public FieldAsGetter(IFieldInfo field, String getterName) {
		this.field = field;
		this.getterName = getterName;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueNullable() {
		return field.isValueNullable();
	}

	@Override
	public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
		return field.getTypeSpecificities();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public String getOnlineHelp() {
		String result = field.getOnlineHelp();
		if (result == null) {
			result = null;
		} else {
			result = "Retrieve: " + result;
		}
		return result;
	}

	@Override
	public String getName() {
		return getterName;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultMethodCaption(this);
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
	public Runnable getUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return field.getType();
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
		return field.getCategory();
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
		FieldAsGetter other = (FieldAsGetter) obj;
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
