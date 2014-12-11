package xy.reflect.ui.info.type;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.TextControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReflectionUIUtils.PrimitiveDefaults;

public class DefaultTextualTypeInfo extends DefaultTypeInfo implements
		ITextualTypeInfo {

	public DefaultTextualTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		if (javaType == null) {
			throw new AssertionError();
		}
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>(
				new DefaultTypeInfo(reflectionUI, javaType).getConstructors());
		if (ReflectionUIUtils.isPrimitiveTypeOrWrapper(javaType)) {
			result.add(new AbstractConstructorMethodInfo(
					DefaultTextualTypeInfo.this) {

				@Override
				public Object invoke(Object object,
						Map<String, Object> valueByParameterName) {
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
		return result;
	}

	@Override
	public String toText(Object value) {
		return value.toString();
	}

	@Override
	public Object fromText(String text) {
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
				throw new AssertionError(e);
			} catch (SecurityException e) {
				throw new AssertionError(e);
			} catch (InstantiationException e) {
				throw new AssertionError(e);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			} catch (InvocationTargetException e) {
				throw new AssertionError(e.getTargetException());
			} catch (NoSuchMethodException e) {
				throw new AssertionError(e);
			}
		}
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new TextControl(reflectionUI, object, field);
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


}
