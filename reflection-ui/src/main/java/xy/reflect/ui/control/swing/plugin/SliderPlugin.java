package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.util.SwingRendererUtils;

public class SliderPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Slider";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Integer.class.equals(javaType) || int.class.equals(javaType);
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

		public Slider(SwingRenderer swingRenderer, IFieldControlInput input, SliderConfiguration controlCustomization) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.controlCustomization = controlCustomization;

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
						data.setValue(Slider.this.getValue());
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

		@Override
		public boolean refreshUI() {
			final int value = ((Number) data.getValue()).intValue();
			listenerDisabled = true;
			try {
				setValue(value);
			} finally {
				listenerDisabled = false;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (value > getMaximum()) {
						SwingRendererUtils.displayErrorOnBorderAndTooltip(Slider.this, Slider.this,
								"The value is greater than the maximum value: " + value + " > " + getMaximum(),
								swingRenderer);
					} else if (value < getMinimum()) {
						SwingRendererUtils.displayErrorOnBorderAndTooltip(Slider.this, Slider.this,
								"The value is less than the minimum value: " + value + " < " + getMinimum(),
								swingRenderer);
					} else {
						SwingRendererUtils.displayErrorOnBorderAndTooltip(Slider.this, Slider.this, null,
								swingRenderer);
					}
				}
			});
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
		public String toString() {
			return "Slider [data=" + data + "]";
		}
	}

}
