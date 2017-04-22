package xy.reflect.ui.info.field;

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
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodAsSubObjectMethod implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String fieldName;

	public MethodAsSubObjectMethod(ReflectionUI reflectionUI, IMethodInfo method, String fieldName) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.fieldName = fieldName;
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
	public boolean isFormControlMandatory() {
		return false;
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
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ITypeInfo getType() {
		return new ValueTypeInfo();
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		ValueInstance result = new ValueInstance(object);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new ValueTypeInfo());
		return result;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
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
		MethodAsSubObjectMethod other = (MethodAsSubObjectMethod) obj;
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
		return "EncapsulatedMethodField [method=" + method + ", fieldName=" + fieldName + "]";
	}

	protected class ValueInstance {

		protected Object object;

		public ValueInstance(Object object) {
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
			ValueInstance other = (ValueInstance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		private MethodAsSubObjectMethod getOuterType() {
			return MethodAsSubObjectMethod.this;
		}

		@Override
		public String toString() {
			return "ValueInstance [of=" + getOuterType() + "]";
		}

	}

	protected class ValueTypeInfo implements ITypeInfo {

		@Override
		public String getName() {
			return "EncapsulatedMethodFieldType [method=" + method.getName() + ", fieldName=" + fieldName + "]";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.composeMessage(method.getCaption(), "Execution");
		}

		@Override
		public String getIconImagePath() {
			return null;
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
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return false;
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
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.<IMethodInfo>singletonList(new MethodInfoProxy(method) {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					object = ((ValueInstance) object).getObject();
					return super.invoke(object, invocationData);
				}

				@Override
				public Runnable getUndoJob(Object object, InvocationData invocationData) {
					object = ((ValueInstance) object).getObject();
					return super.getUndoJob(object, invocationData);
				}

				@Override
				public void validateParameters(Object object, InvocationData invocationData) throws Exception {
					object = ((ValueInstance) object).getObject();
					super.validateParameters(object, invocationData);
				}

			});
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof ValueInstance;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			return ReflectionUIUtils.composeMessage(method.getCaption(), "Execution");
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
			throw new ReflectionUIError();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public String toString() {
			return "EncapsulatedMethodFieldType [method=" + method + ", fieldName=" + fieldName + "]";
		}

	}

}
