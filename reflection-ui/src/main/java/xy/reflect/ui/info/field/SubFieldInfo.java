package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubFieldInfo implements IFieldInfo {

	protected IFieldInfo field;
	protected IFieldInfo theSubField;
	protected ITypeInfo type;

	public SubFieldInfo(ITypeInfo typeInfo, String fieldName, String subFieldName) {
		this.type = typeInfo;
		this.field = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.field == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		this.theSubField = ReflectionUIUtils.findInfoByName(field.getType().getFields(), subFieldName);
		if (this.field == null) {
			throw new ReflectionUIError(
					"Field '" + subFieldName + "' not found in type '" + field.getType().getName() + "'");
		}
	}

	@Override
	public ITypeInfo getType() {
		return theSubField.getType();
	}

	@Override
	public String getName() {
		return SubFieldInfo.class.getSimpleName() + "(" + field.getName() + "." + theSubField.getName() + ")";
	}

	@Override
	public String getCaption() {
		return field.getCaption() + " " + theSubField.getCaption();
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public Object getValue(Object object) {
		Object fieldValue = field.getValue(object);
		if (fieldValue == null) {
			return null;
		}
		return theSubField.getValue(fieldValue);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object fieldValue = field.getValue(object);
		if (fieldValue == null) {
			return null;
		}
		return theSubField.getValueOptions(fieldValue);
	}

	@Override
	public void setValue(Object object, Object subFieldValue) {
		Object fieldValue = field.getValue(object);
		if (subFieldValue == null) {
			if (fieldValue != null) {
				theSubField.setValue(fieldValue, null);
			}
		} else {
			if (fieldValue == null) {
				IMethodInfo fieldCtor = ReflectionUIUtils.getZeroParameterConstrucor(field.getType());
				if (fieldCtor == null) {
					throw new ReflectionUIError(
							"Cannot set sub-field value: Parent field value is null and cannot be constructed: Default constructor not found");
				}
				fieldValue = fieldCtor.invoke(null, new InvocationData());
			}
			theSubField.setValue(fieldValue, subFieldValue);
			if (!field.isGetOnly()) {
				field.setValue(object, fieldValue);
			}
		}
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isNullable() {
		return field.isNullable() || theSubField.isNullable();
	}

	@Override
	public boolean isGetOnly() {
		return theSubField.isGetOnly();
	}

	@Override
	public int hashCode() {
		return field.hashCode() + theSubField.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!field.equals(((SubFieldInfo) obj).field)) {
			return false;
		}
		if (!theSubField.equals(((SubFieldInfo) obj).theSubField)) {
			return false;
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return field.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return theSubField.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}
}
