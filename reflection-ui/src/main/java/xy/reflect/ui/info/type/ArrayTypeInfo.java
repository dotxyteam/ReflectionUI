package xy.reflect.ui.info.type;

import java.lang.reflect.Array;
import java.util.AbstractList;
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
	public List<?> toStandardList(final Object value) {
		return new AbstractList<Object>() {

			@Override
			public Object get(int index) {
				return Array.get(value, index);
			}

			@Override
			public int size() {
				return Array.getLength(value);
			}

			@Override
			public Object set(int index, Object element) {
				Object oldElement = Array.get(value, index);
				Array.set(value, index, element);
				return oldElement;
			}

		};
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
							Map<String, Object> valueByParameterName) {
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
	public Object fromStandardList(List<?> list) {
		Object array = Array.newInstance(itemJavaType, list.size());
		for (int i = 0; i < list.size(); i++) {
			Array.set(array, i, list.get(i));
		}
		return array;
	}

}
