package xy.reflect.ui.info.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.annotation.OnlineHelp;
import xy.reflect.ui.info.annotation.Name;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultParameterInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected Class<?> paramJavaType;
	protected int position;
	protected Member owner;
	protected Annotation[] paramAnnotations;
	protected ITypeInfo type;
	protected String name;

	public DefaultParameterInfo(ReflectionUI reflectionUI, Member owner,
			Class<?> paramJavaType, Annotation[] paramAnnotations, int position) {
		this.reflectionUI = reflectionUI;
		this.owner = owner;
		this.paramJavaType = paramJavaType;
		this.paramAnnotations = paramAnnotations;
		this.position = position;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(getName()) + " ("
				+ getType().getCaption() + ")";
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(
					paramJavaType, owner, position));
		}
		return type;
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		if (name == null) {
			String[] parameterNames = ReflectionUIUtils
					.getJavaParameterNames(owner);
			if (parameterNames == null) {
				for (Annotation annotation : paramAnnotations) {
					if (annotation instanceof Name) {
						return ((Name) annotation).value();
					}
				}
				name = "parameter" + (position + 1);
			} else {
				name = parameterNames[position];
			}
		}
		return name;
	}

	@Override
	public int hashCode() {
		return position;
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
		if (position != ((DefaultParameterInfo) obj).position) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isNullable() {
		return !paramJavaType.isPrimitive();
	}

	@Override
	public Object getDefaultValue() {
		if (paramJavaType.isPrimitive()) {
			return ReflectionUIUtils.PrimitiveDefaults.get(paramJavaType);
		} else {
			return null;
		}
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String getOnlineHelp() {
		for (Annotation annotation : paramAnnotations) {
			if (annotation instanceof OnlineHelp) {
				return ((OnlineHelp) annotation).value();
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
