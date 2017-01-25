package xy.reflect.ui.info.type.custom;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.IReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class TextualTypeInfo extends DefaultTypeInfo {

	public TextualTypeInfo(IReflectionUI reflectionUI, Class<?> javaType) {
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
				return ClassUtils.getDefaultPrimitiveValue(primitiveType);
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
	public String toString() {
		return "TextualTypeInfo [javaType=" + javaType + "]";
	}
	

}
