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

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class SingleLineTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Single Line Text";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return String.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new SingleLineTextConfiguration();
	}

	@Override
	public SingleLineTextControl createControl(Object renderer, IFieldControlInput input) {
		return new SingleLineTextControl((SwingRenderer) renderer, input);
	}

	public static class SingleLineTextConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

	}

	public class SingleLineTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public SingleLineTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			return new JTextField();
		}

		@Override
		protected boolean isMultiline() {
			return false;
		}


		@Override
		public String toString() {
			return "SingleLineTextControl [data=" + data + "]";
		}
	}

}
