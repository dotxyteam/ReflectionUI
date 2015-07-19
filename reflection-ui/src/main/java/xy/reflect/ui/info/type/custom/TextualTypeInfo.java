package xy.reflect.ui.info.type.custom;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.TextControl;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReflectionUIUtils.PrimitiveDefaults;
import xy.reflect.ui.info.method.InvocationData;

public class TextualTypeInfo extends DefaultTypeInfo{

	public TextualTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		if (javaType == null) {
			throw new ReflectionUIError();
		}
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						TextualTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							InvocationData invocationData) {
						if (String.class.equals(javaType)) {
							return new String();
						}
						Class<?> primitiveType = javaType;
						if (ReflectionUIUtils.isPrimitiveWrapper(primitiveType)) {
							primitiveType = ReflectionUIUtils
									.wrapperToPrimitiveType(javaType);
						}
						return PrimitiveDefaults.get(primitiveType);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}

				});
	}

	public static  String toText(Object object) {
		return object.toString();
	}

	public static Object fromText(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ReflectionUIUtils.primitiveToWrapperType(javaType);
		}
		if (javaType == Character.class) {
			text = text.trim();
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

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new TextControl(reflectionUI, object, new FieldInfoProxy(field) {

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
					value = fromText((String) value, javaType);
				}
				super.setValue(object, value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(reflectionUI, String.class);
			}

		});
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return ReflectionUIUtils.isPrimitiveTypeOrWrapperOrString(javaType);
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return true;
	}

	@Override
	public List<IFieldInfo> getFields() {
		return Collections.emptyList();
	}

	@Override
	public List<IMethodInfo> getMethods() {
		return Collections.emptyList();
	}

}
