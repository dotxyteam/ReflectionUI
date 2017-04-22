package xy.reflect.ui.control.swing;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.NumberUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	public PrimitiveValueControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return handleValueConversions(swingRenderer.getReflectionUI(), super.getControlData());
			}
		});
	}

	protected static IFieldControlData handleValueConversions(final ReflectionUI reflectionUI, IFieldControlData data) {
		final Class<?> primitiveWrapperClass;
		try {
			Class<?> dataClass = ClassUtils.getCachedClassforName(data.getType().getName());
			if (dataClass.isPrimitive()) {
				dataClass = ClassUtils.primitiveToWrapperClass(dataClass);
			}
			primitiveWrapperClass = dataClass;
		} catch (ClassNotFoundException e1) {
			throw new ReflectionUIError(e1);
		}
		return new FieldControlDataProxy(data) {

			@Override
			public Object getValue() {
				Object result = super.getValue();
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object value) {
				if (value != null) {
					value = fromText((String) value, primitiveWrapperClass);
				}
				super.setValue(value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(reflectionUI, String.class);
			}

		};
	}

	protected static String toText(Object object) {
		if (object instanceof Number) {
			return NumberFormat.getInstance().format(object);
		} else {
			return ClassUtils.primitiveToString(object);
		}
	}

	protected static Object fromText(String text, Class<?> primitiveWrapperClass) {
		if (Number.class.isAssignableFrom(primitiveWrapperClass)) {
			ParsePosition parseposition = new ParsePosition(0);
			Number result = NumberFormat.getInstance().parse(text, parseposition);
			if (parseposition.getIndex() != (text.length())) {
				throw new NumberFormatException("'" + text + "' is not a valid "
						+ primitiveWrapperClass.getSimpleName().toLowerCase() + " number");
			}
			result = NumberUtils.convertNumberToTargetClass(result, primitiveWrapperClass);
			return result;
		} else {
			return ClassUtils.primitiveFromString(text, primitiveWrapperClass);
		}
	}

	@Override
	public String toString() {
		return "PrimitiveValueControl [data=" + data + "]";
	}

}
