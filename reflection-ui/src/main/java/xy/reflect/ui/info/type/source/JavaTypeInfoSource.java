
package xy.reflect.ui.info.type.source;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.members.ResolvedMethod;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.ArrayTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapAsListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntryTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Type information source for Java types. It extracts {@link ITypeInfo}
 * instances from Java classes.
 * 
 * @author olitank
 *
 */
public class JavaTypeInfoSource implements ITypeInfoSource {

	protected Class<?> javaType;
	protected Member declaringMember;
	protected int declaringInvokableParameterPosition;
	protected Class<?>[] genericTypeParameters;
	protected SpecificitiesIdentifier specificitiesIdentifier;

	public JavaTypeInfoSource(Class<?> javaType, SpecificitiesIdentifier specificitiesIdentifier) {
		this.javaType = javaType;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	public JavaTypeInfoSource(Class<?> javaType, Class<?>[] genericTypeParameters,
			SpecificitiesIdentifier specificitiesIdentifier) {
		this.javaType = javaType;
		this.genericTypeParameters = genericTypeParameters;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	public JavaTypeInfoSource(Class<?> javaType, Member declaringMember, int declaringInvokableParameterPosition,
			SpecificitiesIdentifier specificitiesIdentifier) {
		this.javaType = javaType;
		this.declaringMember = declaringMember;
		this.declaringInvokableParameterPosition = declaringInvokableParameterPosition;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	@Override
	public DefaultTypeInfo buildTypeInfo(ReflectionUI reflectionUI) {
		synchronized (reflectionUI.getTypeCacheMutex()) {
			DefaultTypeInfo result = (DefaultTypeInfo) reflectionUI.getTypeCache().get(this);
			if (result == null) {
				if (StandardCollectionTypeInfo.isCompatibleWith(getJavaType())) {
					Class<?> itemClass = guessGenericTypeParameters(Collection.class, 0);
					ITypeInfo itemType;
					if (itemClass == null) {
						itemType = null;
					} else {
						itemType = reflectionUI.getTypeInfo(new JavaTypeInfoSource(itemClass, null));
					}
					result = new StandardCollectionTypeInfo(reflectionUI, this, itemType);
				} else if (StandardMapAsListTypeInfo.isCompatibleWith(getJavaType())) {
					Class<?> keyClass = guessGenericTypeParameters(Map.class, 0);
					Class<?> valueClass = guessGenericTypeParameters(Map.class, 1);
					result = new StandardMapAsListTypeInfo(reflectionUI, this, keyClass, valueClass);
				} else if (StandardMapEntryTypeInfo.isCompatibleWith(getJavaType())) {
					Class<?> keyClass = null;
					Class<?> valueClass = null;
					Class<?>[] genericParams = getGenericTypeParameters();
					if ((genericParams != null) && (genericParams.length != 2)) {
						throw new ReflectionUIError("Invalid generic type parameter array (expected 2 items) for "
								+ StandardMapEntryTypeInfo.class.getName() + ": " + genericTypeParameters);
					}
					if (genericParams != null) {
						keyClass = genericParams[0];
						valueClass = genericParams[1];
					}
					result = new StandardMapEntryTypeInfo(reflectionUI, this, keyClass, valueClass);
				} else if (getJavaType().isArray()) {
					result = new ArrayTypeInfo(reflectionUI, this);
				} else if (getJavaType().isEnum()) {
					result = new StandardEnumerationTypeInfo(reflectionUI, this);
				} else {
					result = new DefaultTypeInfo(reflectionUI, this);
				}
				reflectionUI.getTypeCache().put(this, result);
			}
			return result;
		}
	}

	@Override
	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return specificitiesIdentifier;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	public Member getDeclaringMember() {
		return declaringMember;
	}

	public int getDeclaringInvokableParameterPosition() {
		return declaringInvokableParameterPosition;
	}

	public Class<?>[] getGenericTypeParameters() {
		return genericTypeParameters;
	}

	public List<Class<?>> guessGenericTypeParameter(Class<?> parameterizedBaseClass) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = null;
		if (declaringMember == null) {
			if (genericTypeParameters != null) {
				resolvedType = typeResolver.resolve(javaType, genericTypeParameters);
			} else {
				resolvedType = typeResolver.resolve(javaType);
			}
		} else {
			MemberResolver memberResolver = new MemberResolver(typeResolver);
			ResolvedType declaringResolvedType = typeResolver.resolve(declaringMember.getDeclaringClass());
			ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(declaringResolvedType, null, null);
			if (declaringMember instanceof Field) {
				ResolvedField[] resolvedFields;
				if (Modifier.isStatic(declaringMember.getModifiers())) {
					resolvedFields = resolvedTypeWithMembers.getStaticFields();
				} else {
					resolvedFields = resolvedTypeWithMembers.getMemberFields();
				}
				for (ResolvedField resolvedField : resolvedFields) {
					if (resolvedField.getRawMember().equals(declaringMember)) {
						resolvedType = resolvedField.getType();
						break;
					}
				}
			} else if (declaringMember instanceof Method) {
				ResolvedMethod[] resolvedMethods;
				if (Modifier.isStatic(declaringMember.getModifiers())) {
					resolvedMethods = resolvedTypeWithMembers.getStaticMethods();
				} else {
					resolvedMethods = resolvedTypeWithMembers.getMemberMethods();
				}
				for (ResolvedMethod resolvedMethod : resolvedMethods) {
					if (resolvedMethod.getRawMember().equals(declaringMember)) {
						if (declaringInvokableParameterPosition == -1) {
							resolvedType = resolvedMethod.getType();
						} else {
							resolvedType = resolvedMethod.getArgumentType(declaringInvokableParameterPosition);
						}
						break;
					}
				}
			} else if (declaringMember instanceof Constructor) {
				for (ResolvedConstructor resolvedConstructor : resolvedTypeWithMembers.getConstructors()) {
					if (resolvedConstructor.getRawMember().equals(declaringMember)) {
						if (declaringInvokableParameterPosition == -1) {
							resolvedType = resolvedConstructor.getType();
						} else {
							resolvedType = resolvedConstructor.getArgumentType(declaringInvokableParameterPosition);
						}
						break;
					}
				}
			} else {
				throw new ReflectionUIError();
			}
			if (resolvedType == null) {
				return null;
			}
		}
		List<Class<?>> result = new ArrayList<Class<?>>();
		List<ResolvedType> resolvedTypeParameters = resolvedType.typeParametersFor(parameterizedBaseClass);
		if (resolvedTypeParameters == null) {
			return null;
		}
		for (ResolvedType classParameter : resolvedTypeParameters) {
			result.add(classParameter.getErasedType());
		}
		return result;
	}

	public Class<?> guessGenericTypeParameters(Class<?> parameterizedBaseClass, int genericParameterIndex) {
		List<Class<?>> parameterClasses = guessGenericTypeParameter(parameterizedBaseClass);
		if (parameterClasses == null) {
			return null;
		}
		if (parameterClasses.size() <= genericParameterIndex) {
			return null;
		}
		return parameterClasses.get(genericParameterIndex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + declaringInvokableParameterPosition;
		result = prime * result + ((declaringMember == null) ? 0 : declaringMember.hashCode());
		result = prime * result + Arrays.hashCode(genericTypeParameters);
		result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
		result = prime * result + ((specificitiesIdentifier == null) ? 0 : specificitiesIdentifier.hashCode());
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
		if (declaringInvokableParameterPosition != other.declaringInvokableParameterPosition)
			return false;
		if (declaringMember == null) {
			if (other.declaringMember != null)
				return false;
		} else if (!declaringMember.equals(other.declaringMember))
			return false;
		if (!Arrays.equals(genericTypeParameters, other.genericTypeParameters))
			return false;
		if (javaType == null) {
			if (other.javaType != null)
				return false;
		} else if (!javaType.equals(other.javaType))
			return false;
		if (specificitiesIdentifier == null) {
			if (other.specificitiesIdentifier != null)
				return false;
		} else if (!specificitiesIdentifier.equals(other.specificitiesIdentifier))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaTypeInfoSource [javaType=" + javaType + ", declaringMember=" + declaringMember
				+ ", declaringInvokableParameterPosition=" + declaringInvokableParameterPosition
				+ ", genericTypeParameters=" + Arrays.toString(genericTypeParameters) + ", specificitiesIdentifier="
				+ specificitiesIdentifier + "]";
	}

}
