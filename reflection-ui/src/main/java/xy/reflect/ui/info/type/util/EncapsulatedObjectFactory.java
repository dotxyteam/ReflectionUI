package xy.reflect.ui.info.type.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory.TypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ArrayAccessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class EncapsulatedObjectFactory {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo fieldType;

	protected String typeCaption = "";
	protected String fieldCaption = "Value";
	protected boolean fieldGetOnly = false;
	protected boolean fieldNullable = true;
	protected ValueReturnMode fieldValueReturnMode = ValueReturnMode.PROXY;
	protected Map<String, Object> fieldSpecificProperties = new HashMap<String, Object>();

	public EncapsulatedObjectFactory(ReflectionUI reflectionUI, ITypeInfo fieldType) {
		this.reflectionUI = reflectionUI;
		this.fieldType = fieldType;
	}

	public Object getInstance(Accessor<Object> fieldValueAccessor) {
		Object value = fieldValueAccessor.get();
		if ((value != null) && !fieldType.supportsInstance(value)) {
			throw new ReflectionUIError();
		}
		Instance result = new Instance(fieldValueAccessor);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	
	public IFieldInfo getValueField() {
		return new ValueField();
	}

	public ITypeInfoSource getInstanceTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
	}

	public String getTypeCaption() {
		return typeCaption;
	}

	public void setTypeCaption(String typeCaption) {
		this.typeCaption = typeCaption;
	}

	public String getFieldCaption() {
		return fieldCaption;
	}

	public void setFieldCaption(String fieldCaption) {
		this.fieldCaption = fieldCaption;
	}

	public boolean isFieldGetOnly() {
		return fieldGetOnly;
	}

	public void setFieldGetOnly(boolean fieldGetOnly) {
		this.fieldGetOnly = fieldGetOnly;
	}

	public boolean isFieldNullable() {
		return fieldNullable;
	}

	public void setFieldNullable(boolean fieldNullable) {
		this.fieldNullable = fieldNullable;
	}

	public ValueReturnMode getFieldValueReturnMode() {
		return fieldValueReturnMode;
	}

	public void setFieldValueReturnMode(ValueReturnMode fieldValueReturnMode) {
		this.fieldValueReturnMode = fieldValueReturnMode;
	}

	public Map<String, Object> getFieldSpecificProperties() {
		return fieldSpecificProperties;
	}

	public void setFieldSpecificProperties(Map<String, Object> fieldSpecificProperties) {
		this.fieldSpecificProperties = fieldSpecificProperties;
	}

	public ITypeInfo getFieldType() {
		return fieldType;
	}

	public Object getInstance(Object[] fieldValueHolder) {
		return getInstance(new ArrayAccessor<Object>(fieldValueHolder));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
		result = prime * result + ((fieldCaption == null) ? 0 : fieldCaption.hashCode());
		result = prime * result + (fieldGetOnly ? 1231 : 1237);
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
		EncapsulatedObjectFactory other = (EncapsulatedObjectFactory) obj;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		if (fieldCaption == null) {
			if (other.fieldCaption != null)
				return false;
		} else if (!fieldCaption.equals(other.fieldCaption))
			return false;
		if (fieldGetOnly != other.fieldGetOnly)
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
		return EncapsulatedObjectFactory.class.getSimpleName() + " [typeCaption=" + typeCaption + ", fieldType="
				+ fieldType + ", fieldCaption=" + fieldCaption + "]";
	}

	protected class TypeInfo implements ITypeInfo {

		@Override
		public String getName() {
			return "Encapsulation[typeCaption=" + typeCaption + ", fieldType=" + fieldType + ", fieldCaption="
					+ fieldCaption + "]";
		}

		@Override
		public String getCaption() {
			return typeCaption;
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
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo> singletonList(getValueField());
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public boolean canCopy(Object object) {
			Instance instance = (Instance) object;
			return ReflectionUIUtils.canCopy(reflectionUI, instance.getValue());
		}

		@Override
		public Object copy(Object object) {
			Instance instance = (Instance) object;
			Object instanceValueCopy = ReflectionUIUtils.copy(reflectionUI, instance.getValue());
			return getInstance(new Object[] { instanceValueCopy });
		}

		@Override
		public boolean equals(Object value1, Object value2) {
			ReflectionUIUtils.checkInstance(this, value1);
			return ReflectionUIUtils.equalsOrBothNull(value1, value2);
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public String toString(Object object) {
			Instance instance = (Instance) object;
			return instance.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			TypeInfo other = (TypeInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			return true;
		}

		private EncapsulatedObjectFactory getOuterType() {
			return EncapsulatedObjectFactory.this;
		}

		@Override
		public String toString() {
			return this.getCaption();
		}

	}

	protected class Instance {
		protected Accessor<Object> fieldValueAccessor;

		public Instance(final Object[] fieldValueHolder) {
			this(new ArrayAccessor<Object>(fieldValueHolder));
		}

		public Instance(Accessor<Object> fieldValueAccessor) {
			super();
			this.fieldValueAccessor = fieldValueAccessor;
		}

		public Object getValue() {
			return fieldValueAccessor.get();
		}

		public void setValue(Object value) {
			fieldValueAccessor.set(value);
		}

		

		@Override
		public String toString() {
			return "Encapsulated [value=" + ReflectionUIUtils.toString(reflectionUI, getValue())  + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldValueAccessor == null) ? 0 : fieldValueAccessor.hashCode());
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
			Instance other = (Instance) obj;
			if (fieldValueAccessor == null) {
				if (other.fieldValueAccessor != null)
					return false;
			} else if (!fieldValueAccessor.equals(other.fieldValueAccessor))
				return false;
			return true;
		}

	}
	
	
	protected class ValueField implements IFieldInfo {
		@Override
		public String getCaption() {
			return fieldCaption;
		}

		@Override
		public void setValue(Object object, Object value) {
			Instance instance = (Instance) object;
			instance.setValue(value);
		}

		@Override
		public boolean isGetOnly() {
			return fieldGetOnly;
		}

		@Override
		public boolean isNullable() {
			return fieldNullable;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return fieldSpecificProperties;
		}

		@Override
		public Object getValue(Object object) {
			Instance instance = (Instance) object;
			return instance.getValue();
		}

		@Override
		public ITypeInfo getType() {
			return fieldType;
		}

		@Override
		public String getName() {
			return "value";
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return fieldValueReturnMode;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		private EncapsulatedObjectFactory getOuterType() {
			return EncapsulatedObjectFactory.this;
		}

		@Override
		public int hashCode() {
			return getOuterType().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ValueField)) {
				return false;
			}
			ValueField other = (ValueField) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return fieldCaption;
		}

	}

}
