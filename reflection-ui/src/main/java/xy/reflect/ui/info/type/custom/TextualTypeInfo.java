package xy.reflect.ui.info.type.custom;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReflectionUIUtils.PrimitiveDefaults;
import xy.reflect.ui.info.method.InvocationData;

public class TextualTypeInfo extends DefaultTypeInfo {

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

	public static boolean isCompatibleWith(Class<?> javaType) {
		return ReflectionUIUtils.isPrimitiveTypeOrWrapperOrString(javaType);
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
