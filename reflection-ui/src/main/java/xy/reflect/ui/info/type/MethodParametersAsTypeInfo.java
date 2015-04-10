package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.EmbeddedFormControl;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;

public class MethodParametersAsTypeInfo implements ITypeInfo {

	protected IMethodInfo method;
	protected ReflectionUI reflectionUI;

	public MethodParametersAsTypeInfo(ReflectionUI reflectionUI,
			IMethodInfo method) {
		this.method = method;
		this.reflectionUI = reflectionUI;
	}

	public IMethodInfo getMethod() {
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
		return result;
	}

	@Override
	public String getName() {
		return method.getName();
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

	public static IFieldInfo getParameterAsField(final IParameterInfo param) {
		return new IFieldInfo() {

			@Override
			public void setValue(Object object, Object value) {
				InstanceInfo instance =  (InstanceInfo) object;
				instance.valueByParameterPosition.put(param.getPosition(), value);
			}

			@Override
			public boolean isNullable() {
				return param.isNullable();
			}

			@Override
			public Object getValue(Object object) {
				InstanceInfo instance =  (InstanceInfo) object;
				if (!instance.valueByParameterPosition.containsKey(param.getPosition())) {
					return param.getDefaultValue();
				}
				return instance.valueByParameterPosition.get(param.getPosition());
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
			public boolean isReadOnly() {
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
			public String getDocumentation() {
				return param.getDocumentation();
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}
		};
	}

	@Override
	public String getDocumentation() {
		return method.getDocumentation();
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
	public boolean isImmutable() {
		return false;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return false;
	}

	@Override
	public String toString(Object object) {
		return method.toString();
	}

	@Override
	public Component createFieldControl(Object object, IFieldInfo field) {
		return new EmbeddedFormControl(reflectionUI, object, field);
	}

	@Override
	public boolean supportsInstance(Object object) {
		return false;
	}

	@Override
	public void validate(Object object) throws Exception {
		InstanceInfo instance = (InstanceInfo) object;
		method.validateParameters(instance.methodOwner,
				instance.valueByParameterPosition);
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	public static class InstanceInfo {
		protected Object methodOwner;
		protected Map<Integer, Object> valueByParameterPosition;

		public InstanceInfo(Object methodowner,
				Map<Integer, Object> valueByParameterPosition) {
			super();
			this.methodOwner = methodowner;
			this.valueByParameterPosition = valueByParameterPosition;
		}
	}

}
