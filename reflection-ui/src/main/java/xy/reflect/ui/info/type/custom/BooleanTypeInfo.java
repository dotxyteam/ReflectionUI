package xy.reflect.ui.info.type.custom;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
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
		return Collections.<IMethodInfo> singletonList(new AbstractConstructorInfo() {

			@Override
			public ITypeInfo getReturnValueType() {
				return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(BooleanTypeInfo.this));
			}

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return ClassUtils.getDefaultPrimitiveValue(boolean.class);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}
		});
	}
	
	public static boolean isCompatibleWith(Class<?> javaType) {
		return (javaType.equals(boolean.class)) || (javaType.equals(Boolean.class));
	}

	@Override
	public String toString() {
		return "BooleanTypeInfo [javaType=" + javaType + "]";
	}
	
	

}
