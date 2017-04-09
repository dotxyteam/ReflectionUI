package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubFieldInfo implements IFieldInfo {

	protected IFieldInfo theField;
	protected IFieldInfo theSubField;

	public SubFieldInfo(IFieldInfo theField, IFieldInfo theSubField) {
		super();
		this.theField = theField;
		this.theSubField = theSubField;
	}

	public SubFieldInfo(ITypeInfo type, String fieldName, String subFieldName) {
		this.theField = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.theField == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		if (this.theField == null) {
			throw new ReflectionUIError(
					"Field '" + subFieldName + "' not found in type '" + theField.getType().getName() + "'");
		}
	}

	@Override
	public ITypeInfo getType() {
		return theSubField.getType();
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubField.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(theField.getCaption(), theSubField.getCaption());
	}

	@Override
	public String toString() {
		return "SubFieldInfo [theField=" + theField + ", theSubField=" + theSubField + "]";
	}

	@Override
	public Object getValue(Object object) {
		Object fieldValue = theField.getValue(object);
		if (fieldValue == null) {
			return null;
		}
		return theSubField.getValue(fieldValue);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object fieldValue = theField.getValue(object);
		if (fieldValue == null) {
			return null;
		}
		return theSubField.getValueOptions(fieldValue);
	}

	@Override
	public void setValue(Object object, Object subFieldValue) {
		Object fieldValue = theField.getValue(object);
		if (subFieldValue == null) {
			if (fieldValue != null) {
				if (!theSubField.isGetOnly()) {
					theSubField.setValue(fieldValue, null);
				}
			}
		} else {
			if (fieldValue == null) {
				IMethodInfo fieldCtor = ReflectionUIUtils.getZeroParameterConstrucor(theField.getType());
				if (fieldCtor == null) {
					throw new ReflectionUIError(
							"Cannot set sub-field value: Parent field value is null and cannot be constructed: Default constructor not found");
				}
				fieldValue = fieldCtor.invoke(null, new InvocationData());
			}
			if (!theSubField.isGetOnly()) {
				theSubField.setValue(fieldValue, subFieldValue);
			}
			if (!theField.isGetOnly()) {
				theField.setValue(object, fieldValue);
			}
		}
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isNullable() {
		return theField.isNullable() || theSubField.isNullable();
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return theField.isGetOnly() || theSubField.isGetOnly();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(theField.getValueReturnMode(), theSubField.getValueReturnMode());
	}

	@Override
	public int hashCode() {
		return theField.hashCode() + theSubField.hashCode();
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
		if (!theField.equals(((SubFieldInfo) obj).theField)) {
			return false;
		}
		if (!theSubField.equals(((SubFieldInfo) obj).theSubField)) {
			return false;
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return theField.getCategory();
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
