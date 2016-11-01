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

public class UtilitiesTypeInfo extends DefaultTypeInfo {
	protected final Object singleton;

	public UtilitiesTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		singleton = new Object() {
			@Override
			public String toString() {
				return UtilitiesTypeInfo.this.toString() + "#" + "SINGLETON";
			}
		};
		reflectionUI.registerPrecomputedTypeInfoObject(singleton, this);
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Object.class.equals(javaType)) {
			return false;
		}
		for (Field field : javaType.getFields()) {
			if (PublicFieldInfo.isCompatibleWith(field)) {
				if (!Modifier.isStatic(field.getModifiers())) {
					return false;
				}
			}
		}
		for (Method method : javaType.getMethods()) {
			if (DefaultMethodInfo.isCompatibleWith(method, javaType)
					|| GetterFieldInfo.isCompatibleWith(method, javaType)) {
				if (!Modifier.isStatic(method.getModifiers())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(UtilitiesTypeInfo.this) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return singleton;
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public boolean supportsInstance(Object object) {
		return (object == singleton) || super.supportsInstance(object);
	}

}
