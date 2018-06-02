package xy.reflect.ui.info.type.factory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodParameterAsFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodInvocationDataAsObjectFactory {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String typeName;

	public MethodInvocationDataAsObjectFactory(ReflectionUI reflectionUI, IMethodInfo method, String typeName) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.typeName = typeName;
	}

	public Instance getInstance(Object object, InvocationData invocationData) {
		Instance result = new MethodInvocationDataAsObjectFactory.Instance(object, invocationData);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public Object unwrapInstanceObject(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (!instance.getOuterType().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getObject();
	}

	public InvocationData unwrapInstanceInvocationData(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (!instance.getOuterType().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getInvocationData();
	}

	public ITypeInfoSource getInstanceTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
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
		MethodInvocationDataAsObjectFactory other = (MethodInvocationDataAsObjectFactory) obj;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
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
		return "MethodSetupObjectFactory [method=" + method + ", typeName=" + typeName + "]";
	}

	public class Instance {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
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
			if (invocationData == null) {
				if (other.invocationData != null)
					return false;
			} else if (!invocationData.equals(other.invocationData))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		private MethodInvocationDataAsObjectFactory getOuterType() {
			return MethodInvocationDataAsObjectFactory.this;
		}

		@Override
		public String toString() {
			return "MethodParametersAsObject [object=" + object + ", invocationData=" + invocationData + "]";
		}

	}

	public class TypeInfo extends AbstractInfo implements ITypeInfo {

		@Override
		public void onFormVisibilityChange(Object object, boolean visible) {
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		@Override
		public MenuModel getMenuModel() {
			return new MenuModel();
		}

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
				result.add(new FieldInfo(param));
			}
			return result;
		}

		@Override
		public String getName() {
			return typeName;
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

		private MethodInvocationDataAsObjectFactory getOuterType() {
			return MethodInvocationDataAsObjectFactory.this;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}

	public class FieldInfo extends MethodParameterAsFieldInfo {

		public FieldInfo(IParameterInfo param) {
			super(MethodInvocationDataAsObjectFactory.this.method, param);
		}

		@Override
		public void setValue(Object object, Object value) {
			Instance instance = (Instance) object;
			instance.invocationData.setParameterValue(param, value);
		}

		@Override
		public Object getValue(Object object) {
			Instance instance = (Instance) object;
			return instance.invocationData.getParameterValue(param);
		}

		private MethodInvocationDataAsObjectFactory getOuterType() {
			return MethodInvocationDataAsObjectFactory.this;
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
			FieldInfo other = (FieldInfo) obj;
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
