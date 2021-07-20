


package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Parameter information extracted from the given object representing a Java
 * parameter.
 * 
 * @author olitank
 *
 */
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
	public String getName() {
		if (name == null) {
			name = javaParameter.getName();
			if (name == Parameter.NO_NAME) {
				name = new DefaultTypeInfo(new JavaTypeInfoSource(reflectionUI, javaParameter.getType(), null))
						.getCaption();
				int sameNameCount = 0;
				int sameNamePosition = 0;
				int parameterposition = 0;
				for (Class<?> c : javaParameter.getDeclaringInvokableParameterTypes()) {
					if (name.equals(new DefaultTypeInfo(new JavaTypeInfoSource(reflectionUI, c, null)).getCaption())) {
						sameNameCount++;
						if (parameterposition < javaParameter.getPosition()) {
							sameNamePosition++;
						}
					}
					parameterposition++;
				}
				if (sameNameCount > 1) {
					name += (sameNamePosition + 1);
				}
				name = name.replace(" ", "");
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
			}
		}
		return name;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(getName());
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, javaParameter.getType(),
					javaParameter.getDeclaringInvokable(), javaParameter.getPosition(), null));
		}
		return type;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public Object getDefaultValue(Object object) {
		if (javaParameter.getType().isPrimitive()) {
			return ClassUtils.getDefaultPrimitiveValue(javaParameter.getType());
		} else {
			return null;
		}
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
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
}
