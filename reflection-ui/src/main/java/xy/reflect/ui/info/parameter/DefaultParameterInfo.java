package xy.reflect.ui.info.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.annotation.Documentation;
import xy.reflect.ui.info.annotation.RuntimeName;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultParameterInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected Class<?> paramJavaType;
	protected int position;
	protected Member owner;
	private Annotation[] paramAnnotations;

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
		return ReflectionUIUtils
				.identifierToCaption(getName())
				+ " ("
				+ getType().getCaption() + ")";
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(paramJavaType,
				owner));
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		String[] parameterNames = ReflectionUIUtils
				.getJavaParameterNames(owner);
		if (parameterNames == null) {
			for(Annotation annotation: paramAnnotations){
				if(annotation instanceof RuntimeName){
					return ((RuntimeName)annotation).value();
				}
			}
			return "parameter" + (position + 1);
		} else {
			return parameterNames[position];
		}
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
	public String getDocumentation() {
		for(Annotation annotation: paramAnnotations){
			if(annotation instanceof Documentation){
				return ((Documentation)annotation).value();
			}
		}
		return null;
	}

}