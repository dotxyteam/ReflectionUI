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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;

/**
 * Field control that displays a check box.
 * 
 * @author olitank
 *
 */
public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	public CheckBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					data.setValue(isSelected());
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(CheckBoxControl.this, t);
				}
			}
		});
		refreshUI(true);
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
			setOpaque(false);
			setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
			setEnabled(!data.isGetOnly());
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		setSelected(Boolean.TRUE.equals(data.getValue()));
		return true;
	}

	@Override
	public boolean isAutoManaged() {
		return false;
	}

	@Override
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		for (Component c : getComponents()) {
			if (SwingRendererUtils.requestAnyComponentFocus(c, swingRenderer)) {
				return true;
			}
		}
		return false;

	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "CheckBoxControl [data=" + data + "]";
	}

}
