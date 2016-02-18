package xy.reflect.ui.info.type.custom;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.util.PrimitiveUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.info.method.InvocationData;

public class BooleanTypeInfo extends DefaultTypeInfo {

	public BooleanTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		if (javaType == null) {
			throw new ReflectionUIError();
		}
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						BooleanTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							InvocationData invocationData) {
						return PrimitiveUtils.getDefaultValue(boolean.class);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return (javaType.equals(boolean.class))
				|| (javaType.equals(Boolean.class));
	}

}
