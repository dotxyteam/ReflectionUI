
package xy.reflect.ui.info.type.iterable;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.method.InvocationData;

/**
 * Type information extracted from the Java array type encapsulated in the given
 * type information source.
 * 
 * @author olitank
 *
 */
public class ArrayTypeInfo extends StandardCollectionTypeInfo {

	public ArrayTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		super(reflectionUI, source,
				reflectionUI.getTypeInfo(new JavaTypeInfoSource(source.getJavaType().getComponentType(), null)));
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
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			@Override
			public ITypeInfo getReturnValueType() {
				return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(ArrayTypeInfo.this, null));
			}

			@Override
			public Object invoke(Object ignore, InvocationData invocationData) {
				return Array.newInstance(getJavaType().getComponentType(), 0);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public ValueReturnMode getItemReturnMode() {
		return source.getJavaType().getComponentType().isPrimitive() ? ValueReturnMode.CALCULATED
				: ValueReturnMode.DIRECT;
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
	public boolean canInstantiateFromArray() {
		return true;
	}

	@Override
	public Object fromArray(Object[] array) {
		Object value = Array.newInstance(getJavaType().getComponentType(), array.length);
		for (int i = 0; i < array.length; i++) {
			Array.set(value, i, array[i]);
		}
		return value;
	}

	@Override
	public boolean areItemsAutomaticallyPositioned() {
		return false;
	}

	@Override
	public String toString() {
		return "ArrayTypeInfo [source=" + source + "]";
	}

}
