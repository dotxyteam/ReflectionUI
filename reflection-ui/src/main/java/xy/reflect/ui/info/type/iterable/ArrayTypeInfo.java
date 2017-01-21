package xy.reflect.ui.info.type.iterable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class ArrayTypeInfo extends StandardCollectionTypeInfo {

	public ArrayTypeInfo(ReflectionUI reflectionUI, Class<?> javaType, Class<?> itemJavaType) {
		super(reflectionUI, javaType, itemJavaType);
	}

	@Override
	public Object[] toArray(final Object listValue) {
		Object[] result = new Object[Array.getLength(listValue)];
		for (int i = 0; i < Array.getLength(listValue); i++) {
			result[i] = Array.get(listValue, i);
		}
		return result;
	}

	@Override
	public String getCaption() {
		ITypeInfo itemType = getItemType();
		if (itemType != null) {
			return "Array Of " + itemType.getCaption();
		} else {
			return "Array";
		}
	}

	@Override
	public boolean isConcrete() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(this) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return Array.newInstance(itemJavaType, 0);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public boolean canReplaceContent() {
		return false;
	}

	@Override
	public void replaceContent(Object listValue, Object[] array) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object fromArray(Object[] array) {
		Object value = Array.newInstance(itemJavaType, array.length);
		for (int i = 0; i < array.length; i++) {
			Array.set(value, i, array[i]);
		}
		return value;
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	@Override
	public String toString() {
		return "ArrayTypeInfo [javaType=" + javaType + "]";
	}

}
