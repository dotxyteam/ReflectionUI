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
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;

import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;

/**
 * This is a sub-class of {@link FieldControlPlaceHolder} that allows to install
 * field customization tools.
 * 
 * @author olitank
 *
 */
public class CustomizingFieldControlPlaceHolder extends FieldControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingFieldControlPlaceHolder(SwingCustomizer swingCustomizer, Form form, IFieldInfo field) {
		super(swingCustomizer, form, field);
	}

	@Override
	public void refreshUI(boolean recreate) {
		refreshInfoCustomizationsControl();
		super.refreshUI(recreate);
	}

	public void refreshInfoCustomizationsControl() {
		if (((SwingCustomizer) swingRenderer)
				.areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
			return;
		}
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForFieldInfo(this);
			add(infoCustomizationsComponent, BorderLayout.EAST);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(infoCustomizationsComponent);
			infoCustomizationsComponent = null;
			refreshInfoCustomizationsControl();
		}
	}

}
