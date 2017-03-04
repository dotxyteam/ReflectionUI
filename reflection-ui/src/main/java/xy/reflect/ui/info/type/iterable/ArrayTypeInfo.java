package xy.reflect.ui.info.type.iterable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class ArrayTypeInfo extends StandardCollectionTypeInfo {

	public ArrayTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		super(reflectionUI, javaType, reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaType.getComponentType())));
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
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo(this) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return Array.newInstance(javaType.getComponentType(), 0);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public boolean isInsertionAllowed() {
		return true;
	}

	@Override
	public boolean isRemovalAllowed() {
		return true;
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
		Object value = Array.newInstance(javaType.getComponentType(), array.length);
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
