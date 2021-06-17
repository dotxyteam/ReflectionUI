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

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

/**
 * Field control that detects and rejects ({@link #refreshUI(boolean)} will
 * return false) unsupported (type not compatible) and null values . A sub-form
 * is used to display the non-null supported value.
 * 
 * Note that this control is used when
 * {@link IFieldControlData#isNullValueDistinct()} returns false. Which means
 * that it prevents its sub-control from encountering null and then displaying a
 * default value. It seems that it is not the expected behavior but fortunately
 * it only happens when the field declared value type is different from the
 * actual value type. The null value in this case allows to destroy the current
 * control, pick a more suitable one and change both the actual type and the
 * control later. Anyway in order to display the default value when null is
 * returned it is still possible to alter the field declared type typically by
 * using a proxy, some customizations, etc.
 * 
 * 
 * @author olitank
 *
 */
public class MutableTypeControl extends NullableControl {

	private static final long serialVersionUID = 1L;

	public MutableTypeControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(super.getControlData());

			@Override
			public IFieldControlData getControlData() {
				return bufferedFieldControlData;
			}
		});
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		Object value = data.getValue();
		if (value == null) {
			return false;
		}
		if (!data.getType().supports(value)) {
			return false;
		}
		((BufferedFieldControlData) data).addInBuffer(value);
		boolean result = super.refreshUI(refreshStructure);
		nullStatusControl.setVisible(false);
		return result;
	}

	@Override
	protected boolean isCaptionDisplayedOnNullStatusControl() {
		return false;
	}

	@Override
	protected IContext getSubContext() {
		return new CustomContext("MutableInstance");
	}

	@Override
	public String toString() {
		return "MutableTypeControl [data=" + data + "]";
	}
}
