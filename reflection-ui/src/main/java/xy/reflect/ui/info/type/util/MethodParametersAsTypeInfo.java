package xy.reflect.ui.info.type.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodParametersAsTypeInfo implements ITypeInfo {

	protected IMethodInfo method;
	protected ReflectionUI reflectionUI;

	public MethodParametersAsTypeInfo(ReflectionUI reflectionUI, IMethodInfo method) {
		this.method = method;
		this.reflectionUI = reflectionUI;
	}

	public IMethodInfo getUnderlyingMethod() {
		return method;
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo> emptyList();
	}

	@Override
	public List<IFieldInfo> getFields() {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(getParameterAsField(param));
		}
		ReflectionUIUtils.sortFields(result);
		return result;
	}

	@Override
	public String getName() {
		return MethodParametersAsTypeInfo.class.getSimpleName() + "(" + method.getName() + ")";
	}

	@Override
	public String getCaption() {
		return method.getCaption();
	}

	@Override
	public int hashCode() {
		return method.hashCode();
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
		return method.equals(((MethodParametersAsTypeInfo) obj).method);
	}

	@Override
	public String toString() {
		return getCaption();
	}

	protected static IFieldInfo getParameterAsField(final IParameterInfo param) {
		return new IFieldInfo() {

			@Override
			public void setValue(Object object, Object value) {
				Instance instance = (Instance) object;
				instance.invocationData.setparameterValue(param, value);
			}

			@Override
			public boolean isNullable() {
				return param.isNullable();
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
			public String getCaption() {
				return param.getCaption();
			}

			@Override
			public boolean isGetOnly() {
				return false;
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
		};
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
		return null;
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
	public boolean equals(Object value1, Object value2) {
		ReflectionUIUtils.checkInstance(this, value1);
		return ReflectionUIUtils.equalsOrBothNull(value1, value2);
	}

	@Override
	public boolean supportsInstance(Object object) {
		return object instanceof Instance;
	}

	@Override
	public void validate(Object object) throws Exception {
		Instance instance = (Instance) object;
		method.validateParameters(instance.methodOwner, instance.invocationData);
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	public Instance getInstance(Object object, InvocationData invocationData) {
		Instance result = new MethodParametersAsTypeInfo.Instance(object, invocationData);
		reflectionUI.registerPrecomputedTypeInfoObject(
				result, this);
		return result;
	}

	protected static class Instance {
		protected Object methodOwner;
		protected InvocationData invocationData;

		public Instance(Object methodowner, InvocationData invocationData) {
			super();
			this.methodOwner = methodowner;
			this.invocationData = invocationData;
		}
	}

}
