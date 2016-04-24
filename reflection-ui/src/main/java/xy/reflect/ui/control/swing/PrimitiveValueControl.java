package xy.reflect.ui.control.swing;

import java.lang.reflect.InvocationTargetException;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.PrimitiveUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	public PrimitiveValueControl(ReflectionUI reflectionUI, Object object,
			IFieldInfo field, Class<? extends Object> primitiveJavaType) {
		super(reflectionUI, object, handleFieldValueConversions(reflectionUI,
				field, primitiveJavaType));
	}

	protected static IFieldInfo handleFieldValueConversions(
			final ReflectionUI reflectionUI, IFieldInfo field,
			final Class<?> primitiveJavaType) {
		return new FieldInfoProxy(field) {

			@Override
			public Object getValue(Object object) {
				Object result = super.getValue(object);
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object object, Object value) {
				if (value != null) {
					value = fromText((String) value, primitiveJavaType);
				}
				super.setValue(object, value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(reflectionUI, String.class);
			}

		};
	}

	public static String toText(Object object) {
		return object.toString();
	}

	public static Object fromText(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = PrimitiveUtils.primitiveToWrapperType(javaType);
		}
		if (javaType == Character.class) {
			if (text.length() != 1) {
				throw new RuntimeException("Invalid value: '" + text
						+ "'. 1 character is expected");
			}
			return text.charAt(0);
		} else {
			try {
				return javaType.getConstructor(new Class[] { String.class })
						.newInstance(text);
			} catch (IllegalArgumentException e) {
				throw new ReflectionUIError(e);
			} catch (SecurityException e) {
				throw new ReflectionUIError(e);
			} catch (InstantiationException e) {
				throw new ReflectionUIError(e);
			} catch (IllegalAccessException e) {
				throw new ReflectionUIError(e);
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e.getTargetException());
			} catch (NoSuchMethodException e) {
				throw new ReflectionUIError(e);
			}
		}
	}
}
