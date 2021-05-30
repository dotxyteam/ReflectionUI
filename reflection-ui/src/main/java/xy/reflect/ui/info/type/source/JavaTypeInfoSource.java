/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
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

	protected ReflectionUI reflectionUI;
	protected Class<?> javaType;
	protected Member declaringMember;
	protected int declaringInvokableParameterPosition;
	protected Class<?>[] genericTypeParameters;
	protected SpecificitiesIdentifier specificitiesIdentifier;

	public JavaTypeInfoSource(ReflectionUI reflectionUI, Class<?> javaType,
			SpecificitiesIdentifier specificitiesIdentifier) {
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	public JavaTypeInfoSource(ReflectionUI reflectionUI, Class<?> javaType, Class<?>[] genericTypeParameters,
			SpecificitiesIdentifier specificitiesIdentifier) {
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
		this.genericTypeParameters = genericTypeParameters;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	public JavaTypeInfoSource(ReflectionUI reflectionUI, Class<?> javaType, Member declaringMember,
			int declaringInvokableParameterPosition, SpecificitiesIdentifier specificitiesIdentifier) {
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
		this.declaringMember = declaringMember;
		this.declaringInvokableParameterPosition = declaringInvokableParameterPosition;
		this.specificitiesIdentifier = specificitiesIdentifier;
	}

	@Override
	public DefaultTypeInfo getTypeInfo() {
		DefaultTypeInfo result;
		if (StandardCollectionTypeInfo.isCompatibleWith(getJavaType())) {
			Class<?> itemClass = guessGenericTypeParameters(Collection.class, 0);
			ITypeInfo itemType;
			if (itemClass == null) {
				itemType = null;
			} else {
				itemType = reflectionUI.getTypeInfo(new JavaTypeInfoSource(reflectionUI, itemClass, null));
			}
			result = new StandardCollectionTypeInfo(this, itemType);
		} else if (StandardMapAsListTypeInfo.isCompatibleWith(getJavaType())) {
			Class<?> keyClass = guessGenericTypeParameters(Map.class, 0);
			Class<?> valueClass = guessGenericTypeParameters(Map.class, 1);
			result = new StandardMapAsListTypeInfo(this, keyClass, valueClass);
		} else if (StandardMapEntryTypeInfo.isCompatibleWith(getJavaType())) {
			Class<?> keyClass = null;
			Class<?> valueClass = null;
			Class<?>[] genericParams = getGenericTypeParameters();
			if (genericParams != null) {
				keyClass = genericParams[0];
				valueClass = genericParams[1];
			}
			result = new StandardMapEntryTypeInfo(this, keyClass, valueClass);
		} else if (getJavaType().isArray()) {
			result = new ArrayTypeInfo(this);
		} else if (getJavaType().isEnum()) {
			result = new StandardEnumerationTypeInfo(this);
		} else {
			result = new DefaultTypeInfo(this);
		}
		return result;
	}

	public ReflectionUI getReflectionUI() {
		return reflectionUI;
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

	public List<Class<?>> guessGenericTypeParameters(Class<?> parameterizedBaseClass) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = null;
		if (declaringMember == null) {
			resolvedType = typeResolver.resolve(javaType);
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
				throw new ReflectionUIError();
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
		List<Class<?>> parameterClasses = guessGenericTypeParameters(parameterizedBaseClass);
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
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
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
