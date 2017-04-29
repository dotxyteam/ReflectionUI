package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.ComponentOrientation;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
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

public class SpinnerPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Spinner";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ClassUtils.primitiveToWrapperClass(javaType);
		}
		return Number.class.isAssignableFrom(javaType);
	}

	@Override
	protected boolean handlesNull() {
		return false;
	}

	@Override
	protected AbstractConfiguration getDefaultControlConfiguration() {
		return new SpinnerConfiguration();
	}

	@Override
	protected Component createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlCustomization) {
		return new Spinner((SwingRenderer) renderer, input, (SpinnerConfiguration) controlCustomization);
	}

	protected static Number parseNumber(String s) {
		try {
			Number result = org.apache.commons.lang3.math.NumberUtils.createNumber(s);
			return result;
		} catch (NumberFormatException e) {
			throw new ReflectionUIError(e);
		}
	}

	public static class SpinnerConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String minimum = "0";
		public String maximum = "100";
		public String stepSize = "1";

		public void validate() {
			parseNumber(minimum);
			parseNumber(maximum);
			parseNumber(stepSize);
		}
	}

	protected class Spinner extends JSpinner implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected SpinnerConfiguration controlCustomization;
		protected boolean listenerDisabled = false;
		protected Class<?> numberClass;

		public Spinner(SwingRenderer swingRenderer, IFieldControlInput input,
				SpinnerConfiguration controlCustomization) {
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

			Number value = (Number) data.getValue();
			Number minimum = getConvertedNumber(controlCustomization.minimum);
			Number maximum = getConvertedNumber(controlCustomization.maximum);
			Number stepSize = getConvertedNumber(controlCustomization.stepSize);
			setModel(new SpinnerNumberModel(value, (Comparable<?>) minimum, (Comparable<?>) maximum, stepSize));

			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((DefaultEditor) getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);

			if (data.isGetOnly()) {
				setEnabled(false);
			} else {
				addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (listenerDisabled) {
							return;
						}
						onSpin();
					}
				});
			}

			refreshUI();
		}

		protected Number getConvertedNumber(String s) {
			Number result = parseNumber(s);
			result = NumberUtils.convertNumberToTargetClass(result, numberClass);
			return result;
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void onSpin() {
			Object value = NumberUtils.convertNumberToTargetClass((Number) Spinner.this.getValue(), numberClass);
			data.setValue(value);
		}

		@Override
		public boolean refreshUI() {
			Number value = (Number) data.getValue();
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
			return "Spinner [data=" + data + "]";
		}
	}

}
