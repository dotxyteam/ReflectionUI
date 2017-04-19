package xy.reflect.ui.control.swing;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ClassUtils;

public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	public PrimitiveValueControl(final SwingRenderer swingRenderer, IFieldControlInput input, final Class<?> javaType) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return handleValueConversions(swingRenderer.getReflectionUI(), super.getControlData(), javaType);
			}

		});
	}

	protected static IFieldControlData handleValueConversions(final ReflectionUI reflectionUI, IFieldControlData data,
			final Class<?> primitiveJavaType) {
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
					value = fromText((String) value, primitiveJavaType);
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
		return object.toString();
	}

	protected static Object fromText(String text, Class<?> javaType) {
		return ClassUtils.primitiveFromText(text, javaType);
	}

	@Override
	public String toString() {
		return "PrimitiveValueControl [data=" + data + "]";
	}

}
