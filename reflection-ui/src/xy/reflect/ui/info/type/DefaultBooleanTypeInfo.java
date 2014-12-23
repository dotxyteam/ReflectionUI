package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.CheckBoxControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIException;
import xy.reflect.ui.util.ReflectionUIUtils.PrimitiveDefaults;

public class DefaultBooleanTypeInfo extends DefaultTypeInfo implements
		IBooleanTypeInfo {

	public DefaultBooleanTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType);
		if (javaType == null) {
			throw new ReflectionUIException();
		}
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						DefaultBooleanTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							Map<String, Object> valueByParameterName) {
						return PrimitiveDefaults.get(boolean.class);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
	}

	@Override
	public Boolean toBoolean(Object value) {
		return (Boolean) value;
	}

	@Override
	public Object fromBoolean(Boolean b) {
		return b;
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new CheckBoxControl(reflectionUI, object, field);
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return (javaType.equals(boolean.class))
				|| (javaType.equals(Boolean.class));
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
