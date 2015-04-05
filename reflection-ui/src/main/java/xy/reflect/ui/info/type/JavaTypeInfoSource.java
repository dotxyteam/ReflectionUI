package xy.reflect.ui.info.type;

import java.lang.reflect.Member;

public class JavaTypeInfoSource implements ITypeInfoSource {

	protected Class<?> javaType;
	protected Member typedMember;
	protected int parameterPosition;

	public JavaTypeInfoSource(Class<?> javaType) {
		this(javaType, null);
	}
	
	public JavaTypeInfoSource(Class<?> javaType, Member typedMember) {
		this(javaType, typedMember, -1);
	}
	
	
	public JavaTypeInfoSource(Class<?> javaType, Member typedMember,
			int parameterPosition) {
		this.javaType = javaType;
		this.typedMember = typedMember;
		this.parameterPosition = parameterPosition;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	public Member getTypedMember() {
		return typedMember;
	}

	public int getParameterPosition() {
		return parameterPosition;
	}

	@Override
	public int hashCode() {
		return javaType.hashCode() + typedMember.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!javaType.equals(((JavaTypeInfoSource) obj).javaType)) {
			return false;
		}
		if (!typedMember.equals(((JavaTypeInfoSource) obj).typedMember)) {
			return false;
		}
		return true;
	}

}
