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

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays primitive (int, float, boolean, char, ...) values
 * in a text box.
 * 
 * @author olitank
 *
 */
public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	protected Throwable currentConversionError;
	protected String currentDataErrorMessage;

	public PrimitiveValueControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected IFieldControlInput adaptTextInput(IFieldControlInput input) {
		return new FieldControlInputProxy(super.adaptTextInput(input)) {
			@Override
			public IFieldControlData getControlData() {
				return handleValueConversions(super.getControlData());
			}
		};
	}

	protected IFieldControlData handleValueConversions(IFieldControlData data) {
		final Class<?> dataClass;
		try {
			dataClass = ClassUtils.getCachedClassforName(data.getType().getName());
		} catch (ClassNotFoundException e1) {
			throw new ReflectionUIError(e1);
		}
		return new FieldControlDataProxy(data) {

			@Override
			public Object getValue() {
				currentConversionError = null;
				updateErrorDisplay();
				Object result = super.getValue();
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object value) {
				if (value != null) {
					try {
						value = fromText((String) value, dataClass);
						currentConversionError = null;
					} catch (Throwable t) {
						currentConversionError = t;
						return;
					} finally {
						updateErrorDisplay();
					}
				}
				super.setValue(value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(new JavaTypeInfoSource(swingRenderer.getReflectionUI(), String.class, null));
			}

		};
	}

	protected void updateErrorDisplay() {
		if (currentConversionError != null) {
			super.displayError(MiscUtils.getPrettyErrorMessage(currentConversionError));
			return;
		}
		if (currentDataErrorMessage != null) {
			super.displayError(currentDataErrorMessage);
			return;
		}
		super.displayError(null);
	}

	@Override
	public boolean displayError(String msg) {
		currentDataErrorMessage = msg;
		updateErrorDisplay();
		return true;
	}

	protected String toText(Object object) {
		return ReflectionUIUtils.primitiveToString(object);
	}

	protected Object fromText(String text, Class<?> dataClass) {
		return ReflectionUIUtils.primitiveFromString(text, dataClass);
	}

	@Override
	public String toString() {
		return "PrimitiveValueControl [data=" + data + "]";
	}

}
