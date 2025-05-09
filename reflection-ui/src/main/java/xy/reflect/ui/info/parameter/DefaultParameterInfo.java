
package xy.reflect.ui.info.parameter;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
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
	protected int position;
	protected Executable javaExecutable;
	protected Class<?> javaObjectType;
	protected String name;

	public DefaultParameterInfo(ReflectionUI reflectionUI, Parameter javaParameter, int position,
			Executable javaExecutable, Class<?> javaObjectType) {
		this.reflectionUI = reflectionUI;
		this.javaParameter = javaParameter;
		this.position = position;
		this.javaExecutable = javaExecutable;
		this.javaObjectType = javaObjectType;
	}

	public Parameter getJavaParameter() {
		return javaParameter;
	}

	public static boolean isCompatibleWith(Parameter javaParameter) {
		return true;
	}

	@Override
	public String getName() {
		if (name == null) {
			if (javaParameter.isNamePresent()) {
				name = javaParameter.getName();
			} else {
				name = reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaParameter.getType(), null)).getCaption();
				int sameNameCount = 0;
				int sameNamePosition = 0;
				int parameterPosition = 0;
				for (Class<?> c : javaExecutable.getParameterTypes()) {
					if (name.equals(new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(c, null)).getCaption())) {
						sameNameCount++;
						if (parameterPosition < position) {
							sameNamePosition++;
						}
					}
					parameterPosition++;
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
		return reflectionUI
				.getTypeInfo(new JavaTypeInfoSource(javaParameter.getType(), javaExecutable, position, null));
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
		return position;
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
		result = prime * result + ((javaExecutable == null) ? 0 : javaExecutable.hashCode());
		result = prime * result + ((javaObjectType == null) ? 0 : javaObjectType.hashCode());
		result = prime * result + ((javaParameter == null) ? 0 : javaParameter.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		if (javaExecutable == null) {
			if (other.javaExecutable != null)
				return false;
		} else if (!javaExecutable.equals(other.javaExecutable))
			return false;
		if (javaObjectType == null) {
			if (other.javaObjectType != null)
				return false;
		} else if (!javaObjectType.equals(other.javaObjectType))
			return false;
		if (javaParameter == null) {
			if (other.javaParameter != null)
				return false;
		} else if (!javaParameter.equals(other.javaParameter))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultParameterInfo [javaParameter=" + javaParameter + ", position=" + position + ", javaMethod="
				+ javaExecutable + ", javaObjectType=" + javaObjectType + "]";
	}

}
