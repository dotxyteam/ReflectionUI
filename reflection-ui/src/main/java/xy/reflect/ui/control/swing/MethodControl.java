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

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Control that displays a button allowing to invoke a method.
 * 
 * @author olitank
 *
 */
public class MethodControl extends AbstractControlButton implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;

	public MethodControl(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		addActionListener(this);
	}

	public Color retrieveBackgroundColor() {
		if (data.getBackgroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getBackgroundColor());
		}
	}

	@Override
	public Color retrieveForegroundColor() {
		if (data.getForegroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getForegroundColor());
		}

	}

	@Override
	public Image retrieveBackgroundImage() {
		if (data.getBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughCache(data.getBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		}
	}

	@Override
	public Color retrieveBorderColor() {
		if (data.getBorderColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getBorderColor());
		}
	}

	@Override
	public String retrieveText() {
		return swingRenderer.prepareMessageToDisplay(
				ReflectionUIUtils.formatMethodControlCaption(data.getCaption(), data.getParameters()));
	}

	@Override
	public String retrieveToolTipText() {
		return swingRenderer.prepareMessageToDisplay(ReflectionUIUtils.formatMethodControlTooltipText(data.getCaption(),
				data.getOnlineHelp(), data.getParameters()));
	}

	@Override
	public Icon retrieveIcon() {
		return swingRenderer.getMethodIcon(data);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		MethodAction action = swingRenderer.createMethodAction(input);
		action.actionPerformed(e);
	}

	@Override
	public String toString() {
		return "MethodControl [data=" + data + "]";
	}

}
