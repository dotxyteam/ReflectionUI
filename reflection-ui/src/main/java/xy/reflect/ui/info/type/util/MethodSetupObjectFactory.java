package xy.reflect.ui.info.type.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodSetupObjectFactory {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String contextId;

	public MethodSetupObjectFactory(ReflectionUI reflectionUI, IMethodInfo method) {
		this.method = method;
		this.reflectionUI = reflectionUI;
	}

	public Instance getInstance(Object object, InvocationData invocationData) {
		Instance result = new MethodSetupObjectFactory.Instance(object, invocationData);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public ITypeInfoSource getInstanceTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
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
		MethodSetupObjectFactory other = (MethodSetupObjectFactory) obj;
		if (contextId == null) {
			if (other.contextId != null)
				return false;
		} else if (!contextId.equals(other.contextId))
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
		return "MethodSetupObjectFactory [method=" + method + ", contextId=" + contextId + "]";
	}

	protected class Instance {
		protected Object object;
		protected InvocationData invocationData;

		public Instance(Object object, InvocationData invocationData) {
			this.object = object;
			this.invocationData = invocationData;
		}

		public Object getObject() {
			return object;
		}

		public InvocationData getInvocationData() {
			return invocationData;
		}

	}

	protected class TypeInfo implements ITypeInfo {

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.<IMethodInfo>emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IParameterInfo param : method.getParameters()) {
				result.add(new ParameterAsField(param));
			}
			ReflectionUIUtils.sortFields(result);
			return result;
		}

		@Override
		public String getName() {
			return "MethodSetupObject [context=" + contextId + "]";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.composeMessage(method.getCaption(), "Execution");
		}

		@Override
		public String getOnlineHelp() {
			return method.getOnlineHelp();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			Instance instance = (Instance) object;
			return method.toString() + "\n<= invoked with: " + instance.invocationData.toString();
		}

		@Override
		public boolean canCopy(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public void validate(Object object) throws Exception {
			Instance instance = (Instance) object;
			method.validateParameters(instance.getObject(), instance.getInvocationData());
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
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

		private MethodSetupObjectFactory getOuterType() {
			return MethodSetupObjectFactory.this;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}

	protected class ParameterAsField implements IFieldInfo {
		protected IParameterInfo param;

		public ParameterAsField(IParameterInfo param) {
			this.param = param;
		}

		@Override
		public void setValue(Object object, Object value) {
			Instance instance = (Instance) object;
			instance.invocationData.setparameterValue(param, value);
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			return null;
		}

		@Override
		public boolean isValueNullable() {
			return param.isValueNullable();
		}

		@Override
		public String getNullValueLabel() {
			return null;
		}

		@Override
		public Object getValue(Object object) {
			Instance instance = (Instance) object;
			return instance.invocationData.getParameterValue(param);
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
		public ITypeInfoProxyFactory getTypeSpecificities() {
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
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public String getOnlineHelp() {
			return param.getOnlineHelp();
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		private MethodSetupObjectFactory getOuterType() {
			return MethodSetupObjectFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			ParameterAsField other = (ParameterAsField) obj;
			if (!getOuterType().equals(other.getOuterType()))
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
			return "Field [of=TypeInfo [of=" + getOuterType() + "]]";
		}
	}

}
