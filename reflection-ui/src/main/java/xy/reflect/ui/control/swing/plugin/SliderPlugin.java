
package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ConversionUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReschedulableTask;

/**
 * Field control plugin that allows to use sliders.
 * 
 * @author olitank
 *
 */
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
		protected ReschedulableTask dataUpdateProcess;

		public Slider(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.dataUpdateProcess = createDelayedUpdateProcess();
			setOpaque(false);
			try {
				this.numberClass = ClassUtils.getCachedClassForName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					try {
						onSlide();
					} catch (Throwable t) {
						swingRenderer.handleException(Slider.this, t);
					}
				}
			});
			addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					try {
						onFocusLoss();
					} catch (Throwable t) {
						swingRenderer.handleException(Slider.this, t);
					}
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
			refreshUI(true);
		}

		protected ReschedulableTask createDelayedUpdateProcess() {
			return swingRenderer.createDelayedUpdateProcess(this, new Runnable() {
				@Override
				public void run() {
					Slider.this.commitChanges();
				}
			}, 750);
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
					if (data.getLabelCustomFontResourcePath() != null) {
						setFont(SwingRendererUtils
								.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
										ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
								.deriveFont(getFont().getStyle(), getFont().getSize()));
					} else {
						setFont(new JSlider().getFont());
					}
					SwingRendererUtils.handleComponentSizeChange(this);
				}
				if (dataUpdateProcess.isActive()) {
					/*
					 * If a change is pending, the refresh can then be aborted, as it will be
					 * performed later after the change is committed. Note that refreshing the
					 * control would have deleted the new control value before it was committed.
					 */
					return true;
				}
				Object value = data.getValue();
				final int intValue;
				if (value == null) {
					intValue = getMinimum();
				} else {
					intValue = (Integer) ConversionUtils.convertNumberToTargetClass((Number) value, Integer.class);
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
				setToolTipText(Integer.toString(intValue));
				return true;
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		public boolean displayError(Throwable error) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void commitChanges() {
			Object value = ConversionUtils.convertNumberToTargetClass(Slider.this.getValue(), numberClass);
			data.setValue(value);
		}

		protected void onSlide() {
			if (listenerDisabled) {
				return;
			}
			dataUpdateProcess.reschedule();
		}

		protected void onFocusLoss() {
			if (dataUpdateProcess.cancelSchedule()) {
				commitChanges();
			}
		}

		@Override
		public boolean isModificationStackManaged() {
			return false;
		}

		@Override
		public boolean areValueAccessErrorsManaged() {
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
		public void validateControlData(ValidationSession session) throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

	}

}
