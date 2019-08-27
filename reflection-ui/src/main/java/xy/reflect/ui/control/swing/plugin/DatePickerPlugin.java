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
package xy.reflect.ui.control.swing.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.JXDatePicker;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.SwingRendererUtils;

public class DatePickerPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Date Picker";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Date.class.isAssignableFrom(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new DatePickerConfiguration();
	}

	@Override
	public DatePicker createControl(Object renderer, IFieldControlInput input) {
		return new DatePicker((SwingRenderer) renderer, input);
	}

	public static class DatePickerConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String format = "yyyy-MM-dd";
	}

	public class DatePicker extends JXDatePicker implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;

		public DatePicker(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setupEvents();
			refreshUI(true);
		}

		protected void setupEvents() {
			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					onNewDate();
				}
			});
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				DatePickerConfiguration controlCustomization = (DatePickerConfiguration) loadControlCustomization(
						input);
				setFormats(controlCustomization.format);
				setEnabled(!data.isGetOnly());
				if (data.getBorderColor() != null) {
					setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				} else {
					setBorder(new JXDatePicker().getBorder());
				}
				if (data.isGetOnly()) {
					getEditor().setBackground(null);
					getEditor().setForeground(null);
				} else {
					if (data.getEditorBackgroundColor() != null) {
						getEditor().setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
					} else {
						getEditor().setBackground(new JXDatePicker().getBackground());
					}
					if (data.getEditorForegroundColor() != null) {
						getEditor().setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
					} else {
						getEditor().setForeground(new JXDatePicker().getForeground());
					}
				}
			}
			Date date = (Date) data.getValue();
			setDate(date);
			return true;

		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void onNewDate() {
			data.setValue(getDate());
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
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
			return "DatePicker [data=" + data + "]";
		}
	}

}
