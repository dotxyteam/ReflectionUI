package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("rawtypes")
public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements
		IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(ReflectionUI reflectionUI,
			Class javaEnumType) {
		super(reflectionUI, javaEnumType);
	}

	@Override
	public Object[] getPossibleValues() {
		return javaType.getEnumConstants();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						StandardEnumerationTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							InvocationData invocationData) {
						return javaType.getEnumConstants()[0];
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}

				});
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new EnumerationControl(reflectionUI, object, field);
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

	@Override
	public String formatEnumerationItem(Object object) {
		if (object == null) {
			return "";
		} else {
			return reflectionUI.toString(object);
		}
	}

}
