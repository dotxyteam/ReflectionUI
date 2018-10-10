package xy.reflect.ui.control.swing.plugin;

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
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	protected AbstractConfiguration getDefaultControlCustomization() {
		return new SpinnerConfiguration();
	}

	@Override
	public Spinner createControl(Object renderer, IFieldControlInput input) {
		return new Spinner((SwingRenderer) renderer, input);
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

	public class Spinner extends JSpinner implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected Class<?> numberClass;

		public Spinner(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((DefaultEditor) getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (listenerDisabled) {
						return;
					}
					onSpin();
				}
			});
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				SpinnerConfiguration controlCustomization = (SpinnerConfiguration) loadControlCustomization(input);
				Number minimum = getConvertedNumber(controlCustomization.minimum);
				Number maximum = getConvertedNumber(controlCustomization.maximum);
				Number stepSize = getConvertedNumber(controlCustomization.stepSize);
				Number value = minimum;
				setModel(new SpinnerNumberModel(value, (Comparable<?>) minimum, (Comparable<?>) maximum, stepSize));
				setEnabled(!data.isGetOnly());
			}
			Number value = (Number) data.getValue();
			if (value == null) {
				value = (Number) ((SpinnerNumberModel) getModel()).getMinimum();
			}
			listenerDisabled = true;
			try {
				setValue(value);
			} finally {
				listenerDisabled = false;
			}
			return true;
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
		public boolean handlesModificationStackAndStress() {
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
			return "Spinner [data=" + data + "]";
		}
	}

}
