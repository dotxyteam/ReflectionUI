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
package xy.reflect.ui.info.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Constructor information extracted from the given Java constructor.
 * 
 * @author olitank
 *
 */
public class DefaultConstructorInfo extends AbstractConstructorInfo {

	protected Constructor<?> javaConstructor;
	protected ReflectionUI reflectionUI;
	protected ArrayList<IParameterInfo> parameters;
	protected ITypeInfo returnValueType;

	public DefaultConstructorInfo(ReflectionUI reflectionUI, Constructor<?> javaConstructor) {
		this.reflectionUI = reflectionUI;
		this.javaConstructor = javaConstructor;
		resolveJavaReflectionModelAccessProblems();
	}

	public static boolean isCompatibleWith(Constructor<?> constructor) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		if (declaringClass.getEnclosingClass() != null) {
			if (!Modifier.isStatic(declaringClass.getModifiers())) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaConstructor.setAccessible(true);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (returnValueType == null) {
			returnValueType = reflectionUI
					.getTypeInfo(new JavaTypeInfoSource(reflectionUI, javaConstructor.getDeclaringClass(), null));
		}
		return returnValueType;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaConstructor.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!DefaultParameterInfo.isCompatibleWith(new Parameter(javaConstructor, i))) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI, new Parameter(javaConstructor, i)));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object parentObject, InvocationData invocationData) {
		Object[] args = new Object[javaConstructor.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param.getPosition());
		}
		try {
			return javaConstructor.newInstance(args);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		} catch (InstantiationException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public int hashCode() {
		return javaConstructor.hashCode();
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
		if (!javaConstructor.equals(((DefaultConstructorInfo) obj).javaConstructor)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultConstructorInfo [javaConstructor=" + javaConstructor + "]";
	}

}
