/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer.CustomizingForm;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizingMethodControlPlaceHolder extends MethodControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingMethodControlPlaceHolder(SwingCustomizer swingCustomizer, CustomizingForm form, IMethodInfo method) {
		super(swingCustomizer, form, method);
	}

	@Override
	public void refreshUI() {
		refreshInfoCustomizationsControl();
		super.refreshUI();
	}

	public void refreshInfoCustomizationsControl() {
		if (((SwingCustomizer) swingRenderer)
				.areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
			return;
		}
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForMethodInfo(this);
			add(infoCustomizationsComponent, BorderLayout.WEST);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(infoCustomizationsComponent);
			infoCustomizationsComponent = null;
			refreshInfoCustomizationsControl();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		if (infoCustomizationsComponent != null) {
			result.width += infoCustomizationsComponent.getPreferredSize().width;
		}
		return result;
	}

}
