package xy.reflect.ui.info.parameter;

import java.lang.reflect.Member;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultParameterInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected Class<?> paramJavaType;
	protected int index;
	protected Member ofMember;

	public DefaultParameterInfo(ReflectionUI reflectionUI, Member ofMember,
			Class<?> paramJavaType, int index) {
		this.reflectionUI = reflectionUI;
		this.ofMember = ofMember;
		this.paramJavaType = paramJavaType;
		this.index = index;
	}

	@Override
	public String getCaption() {
		return getType().getCaption() + " Parameter n°" + (index + 1);
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(paramJavaType,
				ofMember));
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		return "param" + (index + 1);
	}

	@Override
	public int hashCode() {
		return index;
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
		if (index != ((DefaultParameterInfo) obj).index) {
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
		return index;
	}

}
