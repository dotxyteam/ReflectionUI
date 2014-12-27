package xy.reflect.ui.info.field;

import java.util.HashMap;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubFieldInfo implements IFieldInfo {

	protected IFieldInfo field;
	protected IFieldInfo theSubField;
	protected ITypeInfo type;

	public SubFieldInfo(ITypeInfo typeInfo, String fieldName,
			String subFieldName) {
		this.type = typeInfo;
		this.field = ReflectionUIUtils.findInfoByName(type.getFields(),
				fieldName);
		if (this.field == null) {
			throw new ReflectionUIError("Field '" + fieldName
					+ "' not found in type '" + type.getName() + "'");
		}
		this.theSubField = ReflectionUIUtils.findInfoByName(field.getType()
				.getFields(), subFieldName);
		if (this.field == null) {
			throw new ReflectionUIError("Field '" + subFieldName
					+ "' not found in type '" + field.getType().getName() + "'");
		}
	}

	public ITypeInfo getType() {
		return theSubField.getType();
	}

	public String getName() {
		return field.getName() + "." + theSubField.getName();
	}

	public String getCaption() {
		return field.getCaption() + " " + theSubField.getCaption();
	}

	@Override
	public String toString() {
		return getCaption();
	}

	public Object getValue(Object object) {
		Object fieldValue = field.getValue(object);
		if (fieldValue == null) {
			return null;
		}
		return theSubField.getValue(fieldValue);
	}

	public void setValue(Object object, Object value) {
		Object fieldValue = field.getValue(object);
		if (fieldValue == null) {
			fieldValue = getFieldValueConstructor().invoke(null,
					new HashMap<String, Object>());
		}
		theSubField.setValue(fieldValue, value);
		field.setValue(object, fieldValue);
	}

	public boolean isNullable() {
		return field.isNullable() || theSubField.isNullable();
	}

	public boolean isReadOnly() {
		return getFieldValueConstructor() == null;
	}

	public IMethodInfo getFieldValueConstructor() {
		return ReflectionUIUtils.getZeroParameterConstrucor(type);
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
		if(obj == this){
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
	public String getDocumentation() {
		return theSubField.getDocumentation();
	}

}
