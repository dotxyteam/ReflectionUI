package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.NumberUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
	protected boolean handlesNull() {
		return false;
	}

	@Override
	protected AbstractConfiguration getDefaultControlConfiguration() {
		return new SliderConfiguration();
	}

	@Override
	protected Component createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlCustomization) {
		return new Slider((SwingRenderer) renderer, input, (SliderConfiguration) controlCustomization);
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

	protected class Slider extends JSlider implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected SliderConfiguration controlCustomization;
		protected boolean listenerDisabled = false;
		protected Class<?> numberClass;

		public Slider(SwingRenderer swingRenderer, IFieldControlInput input, SliderConfiguration controlCustomization) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.controlCustomization = controlCustomization;
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}

			setMaximum(controlCustomization.maximum);
			setMinimum(controlCustomization.minimum);
			setPaintTicks(controlCustomization.paintTicks);
			setPaintLabels(controlCustomization.paintLabels);
			setMinorTickSpacing(controlCustomization.minorTickSpacing);
			setMajorTickSpacing(controlCustomization.majorTickSpacing);

			if (data.isGetOnly()) {
				setEnabled(false);
			} else {
				addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (listenerDisabled) {
							return;
						}
						onSlide();
					}
				});
			}

			refreshUI();
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void onSlide() {
			Object value = NumberUtils.convertNumberToTargetClass(Slider.this.getValue(), numberClass);
			data.setValue(value);
		}

		@Override
		public boolean refreshUI() {
			final int value = (Integer) NumberUtils.convertNumberToTargetClass((Number) data.getValue(), Integer.class);
			if (value > getMaximum()) {
				throw new ReflectionUIError(
						"The value is greater than the maximum value: " + value + " > " + getMaximum());
			}
			if (value < getMinimum()) {
				throw new ReflectionUIError(
						"The value is less than the minimum value: " + value + " < " + getMinimum());
			}
			listenerDisabled = true;
			try {
				setValue(value);
			} finally {
				listenerDisabled = false;
			}
			return true;
		}

		@Override
		public boolean handlesModificationStackUpdate() {
			return false;
		}

		@Override
		public Object getFocusDetails() {
			return null;
		}

		@Override
		public boolean requestDetailedFocus(Object value) {
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
