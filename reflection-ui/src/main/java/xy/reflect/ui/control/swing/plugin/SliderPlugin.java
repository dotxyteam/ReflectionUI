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
package xy.reflect.ui.control.swing.plugin;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.NumberUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class SliderPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Slider";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Byte.class.equals(javaType) || byte.class.equals(javaType) || Short.class.equals(javaType)
				|| short.class.equals(javaType) || Integer.class.equals(javaType) || int.class.equals(javaType)
				|| Long.class.equals(javaType) || long.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new SliderConfiguration();
	}

	@Override
	public Slider createControl(Object renderer, IFieldControlInput input) {
		return new Slider((SwingRenderer) renderer, input);
	}

	public static class SliderConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;
		public int maximum = 100;
		public int minimum = 0;
		public boolean paintTicks = true;
		public boolean paintLabels = true;
		public int minorTickSpacing = 1;
		public int majorTickSpacing = 10;

	}

	public class Slider extends JSlider implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected Class<?> numberClass;
		protected DelayedUpdateProcess dataUpdateProcess = new DelayedUpdateProcess() {
			@Override
			protected void commit() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Slider.this.commitChanges();
					}
				});
			}

			@Override
			protected long getCommitDelayMilliseconds() {
				return Slider.this.getCommitDelayMilliseconds();
			}
		};

		public Slider(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setOpaque(false);
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					onSlide();
				}
			});
			addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					onFocusLoss();
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			listenerDisabled = true;
			try {
				if (refreshStructure) {
					SliderConfiguration controlCustomization = (SliderConfiguration) loadControlCustomization(input);
					setMaximum(controlCustomization.maximum);
					setMinimum(controlCustomization.minimum);
					setPaintTicks(controlCustomization.paintTicks);
					setPaintLabels(controlCustomization.paintLabels);
					setLabelTable(null);
					setMinorTickSpacing(controlCustomization.minorTickSpacing);
					setMajorTickSpacing(controlCustomization.majorTickSpacing);
					setEnabled(!data.isGetOnly());
					setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					SwingRendererUtils.handleComponentSizeChange(this);
				}
				Object value = data.getValue();
				final int intValue;
				if (value == null) {
					intValue = getMinimum();
				} else {
					intValue = (Integer) NumberUtils.convertNumberToTargetClass((Number) value, Integer.class);
				}
				if (intValue > getMaximum()) {
					throw new ReflectionUIError(
							"The value is greater than the maximum value: " + intValue + " > " + getMaximum());
				}
				if (intValue < getMinimum()) {
					throw new ReflectionUIError(
							"The value is less than the minimum value: " + intValue + " < " + getMinimum());
				}
				setValue(intValue);
				return true;
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected long getCommitDelayMilliseconds() {
			return 750;
		}

		protected void commitChanges() {
			Object value = NumberUtils.convertNumberToTargetClass(Slider.this.getValue(), numberClass);
			data.setValue(value);
		}

		protected void onSlide() {
			if (listenerDisabled) {
				return;
			}
			dataUpdateProcess.cancelCommitSchedule();
			dataUpdateProcess.scheduleCommit();
		}

		protected void onFocusLoss() {
			if (dataUpdateProcess.isCommitScheduled()) {
				dataUpdateProcess.cancelCommitSchedule();
				commitChanges();
			}
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
			return "Slider [data=" + data + "]";
		}
	}

}
