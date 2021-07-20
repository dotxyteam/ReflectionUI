


package xy.reflect.ui.control.swing;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays primitive (int, float, boolean, char, ...) values
 * in a text box.
 * 
 * @author olitank
 *
 */
public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	protected Throwable currentConversionError;
	protected String currentDataErrorMessage;

	public PrimitiveValueControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected IFieldControlInput adaptTextInput(IFieldControlInput input) {
		return new FieldControlInputProxy(super.adaptTextInput(input)) {
			@Override
			public IFieldControlData getControlData() {
				return handleValueConversions(super.getControlData());
			}
		};
	}

	protected IFieldControlData handleValueConversions(IFieldControlData data) {
		final Class<?> dataClass;
		try {
			dataClass = ClassUtils.getCachedClassforName(data.getType().getName());
		} catch (ClassNotFoundException e1) {
			throw new ReflectionUIError(e1);
		}
		return new FieldControlDataProxy(data) {

			@Override
			public Object getValue() {
				currentConversionError = null;
				updateErrorDisplay();
				Object result = super.getValue();
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object value) {
				if (value != null) {
					try {
						value = fromText((String) value, dataClass);
						currentConversionError = null;
					} catch (Throwable t) {
						currentConversionError = t;
						return;
					} finally {
						updateErrorDisplay();
					}
				}
				super.setValue(value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(new JavaTypeInfoSource(swingRenderer.getReflectionUI(), String.class, null));
			}

		};
	}

	protected void updateErrorDisplay() {
		if (currentConversionError != null) {
			super.displayError(MiscUtils.getPrettyErrorMessage(currentConversionError));
			return;
		}
		if (currentDataErrorMessage != null) {
			super.displayError(currentDataErrorMessage);
			return;
		}
		super.displayError(null);
	}

	@Override
	public boolean displayError(String msg) {
		currentDataErrorMessage = msg;
		updateErrorDisplay();
		return true;
	}

	protected String toText(Object object) {
		return ReflectionUIUtils.primitiveToString(object);
	}

	protected Object fromText(String text, Class<?> dataClass) {
		return ReflectionUIUtils.primitiveFromString(text, dataClass);
	}

	@Override
	public String toString() {
		return "PrimitiveValueControl [data=" + data + "]";
	}

}
