package xy.reflect.ui.info.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;

public class UtilitiesTypeInfo extends DefaultTypeInfo {
	protected static final Object NO_INSTANCE = new Object() {
		@Override
		public String toString() {
			return UtilitiesTypeInfo.class.getName() + "#" + "NO_INSTANCE";
		}
	};

	public UtilitiesTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		for (Field field : javaType.getFields()) {
			if (PublicFieldInfo.isCompatibleWith(field)) {
				return false;
			}
		}
		for (Method method : javaType.getMethods()) {
			if (DefaultMethodInfo.isCompatibleWith(method, javaType)) {
				if (!Modifier.isStatic(method.getModifiers())) {
					return false;
				}
			}
			if (GetterFieldInfo.isCompatibleWith(method, javaType)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						UtilitiesTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							InvocationData invocationData) {
						return new PrecomputedTypeInfoInstanceWrapper(
								NO_INSTANCE, UtilitiesTypeInfo.this);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}

				});
	}

}
