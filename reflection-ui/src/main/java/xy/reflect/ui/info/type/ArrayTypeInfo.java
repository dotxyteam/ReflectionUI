package xy.reflect.ui.info.type;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;

public class ArrayTypeInfo extends StandardCollectionTypeInfo {

	public ArrayTypeInfo(ReflectionUI reflectionUI, Class<?> javaType,
			Class<?> itemJavaType) {
		super(reflectionUI, javaType, itemJavaType);
	}

	@Override
	public Object[] toListValue(final Object value) {
		Object[] result = new Object[Array.getLength(value)];
		for (int i = 0; i < Array.getLength(value); i++) {
			result[i] = Array.get(value, i);
		}
		return result;
	}

	@Override
	public String toString() {
		return itemJavaType + "[]";
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						new ArrayTypeInfo(reflectionUI, javaType, itemJavaType)) {

					@Override
					public Object invoke(Object object,
							Map<Integer, Object> valueByParameterPosition) {
						return Array.newInstance(itemJavaType, 0);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}

				});
	}

	@Override
	public String getCaption() {
		return "Array of " + getItemType().getCaption();
	}

	@Override
	public Object fromListValue(Object[] listValue) {
		Object array = Array.newInstance(itemJavaType, listValue.length);
		for (int i = 0; i < listValue.length; i++) {
			Array.set(array, i, listValue[i]);
		}
		return array;
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	
	
}
