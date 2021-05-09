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
package xy.reflect.ui.control.swing;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control that displays primitive (int, float, boolean, char, ...) values
 * in a text box.
 * 
 * @author olitank
 *
 */
public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	public PrimitiveValueControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return handleValueConversions(swingRenderer.getReflectionUI(), super.getControlData());
			}
		});
	}

	protected static IFieldControlData handleValueConversions(final ReflectionUI reflectionUI, IFieldControlData data) {
		final Class<?> primitiveWrapperClass;
		try {
			Class<?> dataClass = ReflectionUtils.getCachedClassforName(data.getType().getName());
			if (dataClass.isPrimitive()) {
				dataClass = ReflectionUtils.primitiveToWrapperClass(dataClass);
			}
			primitiveWrapperClass = dataClass;
		} catch (ClassNotFoundException e1) {
			throw new ReflectionUIError(e1);
		}
		return new FieldControlDataProxy(data) {

			@Override
			public Object getValue() {
				Object result = super.getValue();
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object value) {
				if (value != null) {
					value = fromText((String) value, primitiveWrapperClass);
				}
				super.setValue(value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(String.class, null));
			}

		};
	}

	protected static String toText(Object object) {
		return ReflectionUtils.primitiveToString(object);
	}

	protected static Object fromText(String text, Class<?> primitiveWrapperClass) {
		return ReflectionUtils.primitiveFromString(text, primitiveWrapperClass);
	}

	@Override
	public String toString() {
		return "PrimitiveValueControl [data=" + data + "]";
	}

}
