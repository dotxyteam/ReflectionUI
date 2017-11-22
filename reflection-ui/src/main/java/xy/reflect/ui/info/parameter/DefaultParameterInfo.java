package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultParameterInfo extends AbstractInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected Parameter javaParameter;
	protected ITypeInfo type;
	protected String name;

	public static boolean isCompatibleWith(Parameter javaParameter) {
		return true;
	}

	public DefaultParameterInfo(ReflectionUI reflectionUI, Parameter javaParameter) {
		this.reflectionUI = reflectionUI;
		this.javaParameter = javaParameter;
	}

	@Override
	public String getCaption() {
		String result = ReflectionUIUtils.getDefaultFieldCaption(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
			@Override
			public ITypeInfo getType() {
				return DefaultParameterInfo.this.getType();
			}

			@Override
			public String getName() {
				return DefaultParameterInfo.this.getName();
			}
		});
		if (javaParameter.getName() == Parameter.NO_NAME) {
			result += " (" + getType().getCaption() + ")";
		}
		return result;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaParameter.getType(),
					javaParameter.getDeclaringInvokable(), javaParameter.getPosition()));
		}
		return type;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = javaParameter.getName();
			if (name == Parameter.NO_NAME) {
				name = "parameter" + (javaParameter.getPosition() + 1);
			}
		}
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaParameter == null) ? 0 : javaParameter.hashCode());
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
		DefaultParameterInfo other = (DefaultParameterInfo) obj;
		if (javaParameter == null) {
			if (other.javaParameter != null)
				return false;
		} else if (!javaParameter.equals(other.javaParameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultParameterInfo [javaParameter=" + javaParameter + "]";
	}

	@Override
	public boolean isNullValueDistinct() {
		return !getType().isPrimitive();
	}

	@Override
	public Object getDefaultValue() {
		if (javaParameter.getType().isPrimitive()) {
			return ClassUtils.getDefaultPrimitiveValue(javaParameter.getType());
		} else {
			return null;
		}
	}

	@Override
	public int getPosition() {
		return javaParameter.getPosition();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
