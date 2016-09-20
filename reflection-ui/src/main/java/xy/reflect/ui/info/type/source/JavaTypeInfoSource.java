package xy.reflect.ui.info.type.source;

import java.lang.reflect.Member;

public class JavaTypeInfoSource implements ITypeInfoSource {

	protected Class<?> javaType;
	protected Member typedMember;
	protected int parameterPosition;
	protected Class<?>[] genericTypeParameters;

	public JavaTypeInfoSource(Class<?> javaType) {
		this.javaType = javaType;
	}
	
	public JavaTypeInfoSource(Class<?> javaType, Class<?>... genericTypeParameters) {
		this.javaType = javaType;
		this.genericTypeParameters = genericTypeParameters;
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

	public Class<?>[] getGenericTypeParameters() {
		return genericTypeParameters;
	}

	public void setGenericTypeParameters(Class<?>[] genericTypeParameters) {
		this.genericTypeParameters = genericTypeParameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((javaType == null) ? 0 : javaType.hashCode());
		result = prime * result + parameterPosition;
		result = prime * result
				+ ((typedMember == null) ? 0 : typedMember.hashCode());
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
		JavaTypeInfoSource other = (JavaTypeInfoSource) obj;
		if (javaType == null) {
			if (other.javaType != null)
				return false;
		} else if (!javaType.equals(other.javaType))
			return false;
		if (parameterPosition != other.parameterPosition)
			return false;
		if (typedMember == null) {
			if (other.typedMember != null)
				return false;
		} else if (!typedMember.equals(other.typedMember))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaTypeInfoSource [javaType=" + javaType + "]";
	}

	
}
