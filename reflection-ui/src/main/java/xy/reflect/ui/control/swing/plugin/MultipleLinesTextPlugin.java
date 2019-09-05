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

import java.awt.Dimension;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class MultipleLinesTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Multiple Lines Text";
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
		return new MultipleLinesTextConfiguration();
	}

	@Override
	public MultipleLinesTextControl createControl(Object renderer, IFieldControlInput input) {
		return new MultipleLinesTextControl((SwingRenderer) renderer, input);
	}

	public static class MultipleLinesTextConfiguration extends AbstractConfiguration {

		private static final long serialVersionUID = 1L;

		public ControlDimensionSpecification length = new ControlDimensionSpecification();

		public int getLenghthInPixels() {
			if (length.unit == ControlSizeUnit.PIXELS) {
				return length.value;
			} else if (length.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = SwingRendererUtils.getDefaultScreenSize();
				return Math.round((length.value / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}

		public enum ControlSizeUnit {
			PIXELS, SCREEN_PERCENT
		}

		public static class ControlDimensionSpecification implements Serializable {

			private static final long serialVersionUID = 1L;

			public int value = 40;
			public ControlSizeUnit unit = ControlSizeUnit.SCREEN_PERCENT;

		}

	}

	public class MultipleLinesTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public MultipleLinesTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected Dimension getScrollPaneSize(JScrollPane scrollPane, Dimension defaultSize) {
			Dimension result = super.getScrollPaneSize(scrollPane, defaultSize);
			MultipleLinesTextConfiguration controlCustomization = (MultipleLinesTextConfiguration) loadControlCustomization(
					input);
			int configuredHeight = controlCustomization.getLenghthInPixels();
			result.height = configuredHeight;
			return result;
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
			super.refreshUI(refreshStructure);
			if (refreshStructure) {
				if (data.getCaption().length() > 0) {
					setBorder(
							BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
					if (data.getLabelForegroundColor() != null) {
						((TitledBorder) getBorder())
								.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					}
					if (data.getBorderColor() != null) {
						((TitledBorder) getBorder()).setBorder(
								BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					}
				} else {
					setBorder(BorderFactory.createEmptyBorder());
				}
				textComponent.setBorder(BorderFactory.createEmptyBorder());
			}
			return true;
		}

		@Override
		public String toString() {
			return "MultipleLinesTextControl [data=" + data + "]";
		}
	}

}
