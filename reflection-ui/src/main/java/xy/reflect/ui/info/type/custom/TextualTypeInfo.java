package xy.reflect.ui.info.type.custom;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class TextualTypeInfo extends DefaultTypeInfo {

	public TextualTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		if (javaType == null) {
			throw new ReflectionUIError();
		}
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(TextualTypeInfo.this) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				if (String.class.equals(javaType)) {
					return new String();
				}
				Class<?> primitiveType = javaType;
				if (ClassUtils.isPrimitiveWrapper(primitiveType)) {
					primitiveType = ClassUtils.wrapperToPrimitiveType(javaType);
				}
				return ClassUtils.getDefaultValue(primitiveType);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return ClassUtils.isPrimitiveTypeOrWrapperOrString(javaType);
	}

	@Override
	public Component createCustomNonNullFieldValueControl(Object object, IFieldInfo field) {
		if (javaType == String.class) {
			return new TextControl(reflectionUI, object, field);
		} else {
			return new PrimitiveValueControl(reflectionUI, object, field, javaType);
		}
	}
	
	@Override
	public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		return true;
	}
	

}
