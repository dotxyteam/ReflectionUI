package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class VirtualFieldWrapperTypeInfo implements ITypeInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo fieldType;
	protected String fieldCaption;
	protected boolean fieldReadOnly;
	protected String caption;

	public VirtualFieldWrapperTypeInfo(ReflectionUI reflectionUI, ITypeInfo fieldType,
			String fieldCaption, boolean fieldReadOnly, String caption) {
		this.reflectionUI = reflectionUI;
		this.fieldType = fieldType;
		this.fieldCaption = fieldCaption;
		this.fieldReadOnly = fieldReadOnly;
		this.caption = caption;
	}

	@Override
	public String getName() {
		return VirtualFieldWrapperTypeInfo.class.getSimpleName() + "(" + fieldCaption
				+ ")";
	}

	@Override
	public String getCaption() {
		return caption;
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
	public List<IMethodInfo> getConstructors() {
		return Collections.emptyList();
	}

	@Override
	public List<IFieldInfo> getFields() {
		return Collections.<IFieldInfo> singletonList(getValueField());
	}

	public IFieldInfo getValueField() {
		return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

			@Override
			public String getCaption() {
				return fieldCaption;
			}

			@Override
			public void setValue(Object object, Object value) {
				InstanceInfo instance = (InstanceInfo) object;
				instance.setValue(value);
			}

			@Override
			public boolean isGetOnly() {
				return fieldReadOnly;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public Object getValue(Object object) {
				InstanceInfo instance = (InstanceInfo) object;
				return instance.getValue();
			}

			@Override
			public ITypeInfo getType() {
				return fieldType;
			}

			@Override
			public String toString() {
				return fieldCaption;
			}
			
			
		};
	}

	@Override
	public List<IMethodInfo> getMethods() {
		return Collections.emptyList();
	}

	@Override
	public boolean supportsInstance(Object object) {
		return object instanceof InstanceInfo;
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return null;
	}

	@Override
	public String toString(Object object) {
		InstanceInfo instance = (InstanceInfo) object;
		return reflectionUI.toString(instance.getValue());
	}

	@Override
	public Image getIconImage(Object object) {
		InstanceInfo instance = (InstanceInfo) object;
		return reflectionUI.getIconImage(instance.getValue());
	}

	@Override
	public void validate(Object object) throws Exception {
		InstanceInfo instance = (InstanceInfo) object;
		fieldType.validate(instance.getValue());
	}

	public Object getPrecomputedTypeInfoInstanceWrapper(Object[] fieldValueHolder) {
		if (!fieldType.supportsInstance(fieldValueHolder[0])) {
			throw new ReflectionUIError();
		}
		return new PrecomputedTypeInfoInstanceWrapper(new InstanceInfo(
				fieldValueHolder), this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((caption == null) ? 0 : caption.hashCode());
		result = prime * result
				+ ((fieldCaption == null) ? 0 : fieldCaption.hashCode());
		result = prime * result + (fieldReadOnly ? 1231 : 1237);
		result = prime * result
				+ ((fieldType == null) ? 0 : fieldType.hashCode());
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
		VirtualFieldWrapperTypeInfo other = (VirtualFieldWrapperTypeInfo) obj;
		if (caption == null) {
			if (other.caption != null)
				return false;
		} else if (!caption.equals(other.caption))
			return false;
		if (fieldCaption == null) {
			if (other.fieldCaption != null)
				return false;
		} else if (!fieldCaption.equals(other.fieldCaption))
			return false;
		if (fieldReadOnly != other.fieldReadOnly)
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
		return getCaption();
	}

	public static Object wrap(ReflectionUI reflectionUI,
			final Object[] fieldValueHolder, final String fieldCaption,
			final String wrapperTypeCaption, final boolean readOnly) {
		final ITypeInfo fieldType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(fieldValueHolder[0]));
		VirtualFieldWrapperTypeInfo wrapperType = new VirtualFieldWrapperTypeInfo(
				reflectionUI, fieldType, fieldCaption, readOnly,
				wrapperTypeCaption);
		return wrapperType
				.getPrecomputedTypeInfoInstanceWrapper(fieldValueHolder);
	}

	protected static class InstanceInfo {
		protected Object[] fieldValueHolder;

		public InstanceInfo(Object[] fieldValueHolder) {
			super();
			this.fieldValueHolder = fieldValueHolder;
		}
		
		public Object getValue(){
			return fieldValueHolder[0];
		}
		
		public void setValue(Object value){
			fieldValueHolder[0] = value;
		}

		@Override
		public String toString() {
			Object value = getValue();
			if(value == null){
				return null;
			}
			return value.toString();
		}
		
		
	}

	@Override
	public Component createFieldControl(Object object, IFieldInfo field) {
		return new EmbeddedFormControl(reflectionUI, object, field);
	}

	@Override
	public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		return false;
	}
}
