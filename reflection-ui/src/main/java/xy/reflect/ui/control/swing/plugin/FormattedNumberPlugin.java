
package xy.reflect.ui.control.swing.plugin;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.text.NumberFormatter;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that allows to display formatted numbers.
 * 
 * @author olitank
 *
 */
public class FormattedNumberPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Formatted Number";
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
	public AbstractConfiguration getDefaultControlCustomization() {
		return new FormattedNumberConfiguration();
	}

	@Override
	public FormattedNumberControl createControl(Object renderer, IFieldControlInput input) {
		return new FormattedNumberControl((SwingRenderer) renderer, input);
	}

	public static class FormattedNumberConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String pattern = new DecimalFormat().toPattern();

	}

	public class FormattedNumberControl extends PrimitiveValueControl {

		private static final long serialVersionUID = 1L;

		protected IFieldControlInput numberInput;

		public FormattedNumberControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected IFieldControlInput adaptTextInput(IFieldControlInput input) {
			numberInput = input;
			return super.adaptTextInput(input);
		}

		@Override
		protected String toText(Object object) {
			Class<?> javaType = object.getClass();
			try {
				return getNumberFormatter(javaType).valueToString(object);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		protected NumberFormatter getNumberFormatter(Class<?> javaType) {
			FormattedNumberConfiguration controlCustomization = (FormattedNumberConfiguration) loadControlCustomization(
					numberInput);
			return ReflectionUIUtils.getDefaultNumberFormatter(javaType,
					new DecimalFormat(controlCustomization.pattern));
		}

		@Override
		protected Object fromText(String text, Class<?> dataClass) {
			try {
				return getNumberFormatter(dataClass).stringToValue(text);
			} catch (ParseException e) {
				throw new ReflectionUIError(dataClass.getSimpleName() + " Inupt Error: " + e.toString(), e);
			}
		}

		@Override
		public String toString() {
			return "FormattedNumberControl [data=" + data + "]";
		}
	}

}
