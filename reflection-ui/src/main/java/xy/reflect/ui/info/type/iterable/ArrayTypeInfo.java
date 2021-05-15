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
package xy.reflect.ui.info.type.iterable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.method.InvocationData;

/**
 * Type information extracted from the Java array type encapsulated in the given
 * type information source.
 * 
 * @author olitank
 *
 */
public class ArrayTypeInfo extends StandardCollectionTypeInfo {

	public ArrayTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		super(reflectionUI, source, reflectionUI
				.getTypeInfo(new JavaTypeInfoSource(reflectionUI, source.getJavaType().getComponentType(), null)));
	}

	@Override
	public Object[] toArray(final Object listValue) {
		Object[] result = new Object[Array.getLength(listValue)];
		for (int i = 0; i < Array.getLength(listValue); i++) {
			result[i] = Array.get(listValue, i);
		}
		return result;
	}

	@Override
	public String getCaption() {
		ITypeInfo itemType = getItemType();
		if (itemType != null) {
			return "Array Of " + itemType.getCaption();
		} else {
			return "Array";
		}
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			ITypeInfo returnValueType;

			@Override
			public ITypeInfo getReturnValueType() {
				if (returnValueType == null) {
					returnValueType = reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(ArrayTypeInfo.this, null));
				}
				return returnValueType;
			}

			@Override
			public Object invoke(Object ignore, InvocationData invocationData) {
				return Array.newInstance(getJavaType().getComponentType(), 0);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public boolean isInsertionAllowed() {
		return true;
	}

	@Override
	public boolean isRemovalAllowed() {
		return true;
	}

	@Override
	public boolean canReplaceContent() {
		return false;
	}

	@Override
	public void replaceContent(Object listValue, Object[] array) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canInstanciateFromArray() {
		return true;
	}

	@Override
	public Object fromArray(Object[] array) {
		Object value = Array.newInstance(getJavaType().getComponentType(), array.length);
		for (int i = 0; i < array.length; i++) {
			Array.set(value, i, array[i]);
		}
		return value;
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	@Override
	public String toString() {
		return "ArrayTypeInfo [source=" + source + "]";
	}

}
