package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MultipleMembersAsField implements IFieldInfo {

	protected List<IFieldInfo> encapsulatedFields;
	protected List<IMethodInfo> encapsulatedMethods;
	protected ReflectionUI reflectionUI;
	protected String fieldName;
	protected String contextId;

	public MultipleMembersAsField(ReflectionUI reflectionUI, String fieldName, List<IFieldInfo> encapsulatedFields,
			List<IMethodInfo> encapsulatedMethods, String contextId) {
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
		this.encapsulatedFields = encapsulatedFields;
		this.encapsulatedMethods = encapsulatedMethods;
		this.contextId = contextId;
	}

	public List<IFieldInfo> getEncapsulatedFields() {
		return encapsulatedFields;
	}

	public List<IMethodInfo> getEncapsulatedMethods() {
		return encapsulatedMethods;
	}

	public void setEncapsulatedFields(List<IFieldInfo> encapsulatedFields) {
		this.encapsulatedFields = encapsulatedFields;
	}

	public void setEncapsulatedMethods(List<IMethodInfo> encapsulatedMethods) {
		this.encapsulatedMethods = encapsulatedMethods;
	}

	public String getContextId() {
		return contextId;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(new ValueTypeInfo()));
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public Object getValue(Object object) {
		return getInstance(object);
	}

	public Object getInstance(Object object) {
		Instance result = new Instance(object);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new ValueTypeInfo());
		return result;
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isValueNullable() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
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
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((encapsulatedFields == null) ? 0 : encapsulatedFields.hashCode());
		result = prime * result + ((encapsulatedMethods == null) ? 0 : encapsulatedMethods.hashCode());
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
		MultipleMembersAsField other = (MultipleMembersAsField) obj;
		if (contextId == null) {
			if (other.contextId != null)
				return false;
		} else if (!contextId.equals(other.contextId))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (encapsulatedFields == null) {
			if (other.encapsulatedFields != null)
				return false;
		} else if (!encapsulatedFields.equals(other.encapsulatedFields))
			return false;
		if (encapsulatedMethods == null) {
			if (other.encapsulatedMethods != null)
				return false;
		} else if (!encapsulatedMethods.equals(other.encapsulatedMethods))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MultipleMembersAsField [fieldName=" + fieldName + ", contextId=" + contextId + ", fields="
				+ encapsulatedFields + ", methods=" + encapsulatedMethods + "]";
	}

	protected class Instance {

		protected Object object;

		public Instance(Object object) {
			super();
			this.object = object;
		}

		public Object getObject() {
			return object;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		private MultipleMembersAsField getOuterType() {
			return MultipleMembersAsField.this;
		}

		@Override
		public String toString() {
			return "MultipleMembersAsFieldInstance [object=" + object + "]";
		}

	}

	protected class ValueTypeInfo implements ITypeInfo {

		@Override
		public String getName() {
			return "MultipleMembersAsFieldType [context=" + contextId + ", fieldName=" + fieldName + "]";
		}

		@Override
		public String getCaption() {
			return MultipleMembersAsField.this.getCaption();
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
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return false;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : MultipleMembersAsField.this.encapsulatedFields) {
				result.add(new FieldProxy(field));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : MultipleMembersAsField.this.encapsulatedMethods) {
				result.add(new MethodProxy(method));
			}
			return result;
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
		public String toString(Object object) {
			return object.toString();
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public boolean canCopy(Object object) {
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public String getIconImagePath() {
			return null;
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		protected MultipleMembersAsField getOuterType() {
			return MultipleMembersAsField.this;
		}

		@Override
		public String toString() {
			return "ValueTypeInfo [of=" + getOuterType() + "]";
		}

	}

	public class FieldProxy extends FieldInfoProxy {

		public FieldProxy(IFieldInfo base) {
			super(base);
		}

		@Override
		public Object getValue(Object object) {
			object = ((Instance) object).getObject();
			return super.getValue(object);
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			object = ((Instance) object).getObject();
			return super.getCustomUndoUpdateJob(object, value);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			object = ((Instance) object).getObject();
			return super.getValueOptions(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			object = ((Instance) object).getObject();
			super.setValue(object, value);
		}

	}

	public class MethodProxy extends MethodInfoProxy {

		public MethodProxy(IMethodInfo base) {
			super(base);
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			object = ((Instance) object).getObject();
			return super.invoke(object, invocationData);
		}

		@Override
		public Runnable getUndoJob(Object object, InvocationData invocationData) {
			object = ((Instance) object).getObject();
			return super.getUndoJob(object, invocationData);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			object = ((Instance) object).getObject();
			super.validateParameters(object, invocationData);
		}

	}

}
