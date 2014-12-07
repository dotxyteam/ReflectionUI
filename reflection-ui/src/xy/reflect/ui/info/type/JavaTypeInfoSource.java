package xy.reflect.ui.info.type;

import java.lang.reflect.Member;

public class JavaTypeInfoSource implements ITypeInfoSource {

	protected Class<?> javaType;
	protected Member ofMember ;

	public JavaTypeInfoSource(Class<?> clazz) {
		this(clazz, null);
	}
		
	public JavaTypeInfoSource(Class<?> clazz, Member ofMember) {
		super();
		this.javaType = clazz;
		this.ofMember = ofMember;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	
	public Member ofMember() {
		return ofMember;
	}

	@Override
	public int hashCode() {
		return javaType.hashCode() + ofMember.hashCode();
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
		if (!ofMember.equals(((JavaTypeInfoSource) obj).ofMember)) {
			return false;
		}
		return true;
	}


}
