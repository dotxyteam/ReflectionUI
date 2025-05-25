
package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field proxy allowing to view/edit the base field value as a singleton list.
 * 
 * @author olitank
 *
 */
public class ValueAsListFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo objectType;

	public ValueAsListFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo objectType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.objectType = objectType;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(createListType().getSource());
	}

	protected IListTypeInfo createListType() {
		return new ListTypeInfo();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		if (value != null) {
			value = new ArrayList(Arrays.asList(value));
		}
		return value;
	}

	@Override
	public void setValue(Object object, Object value) {
		if (value != null) {
			ArrayList<?> list = (ArrayList<?>) value;
			if (list.size() != 1) {
				throw new ReflectionUIError("The list size must be equal to 1");
			}
			value = list.get(0);
		}
		super.setValue(object, value);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		List<IMethodInfo> result = super.getAlternativeConstructors(object);
		if (result != null) {
			result = result.stream().map(ctor -> fromValueToListConstructor(ctor)).collect(Collectors.toList());
		}
		return result;
	}

	protected IMethodInfo fromValueToListConstructor(IMethodInfo ctor) {
		return new MethodInfoProxy(ctor) {

			@Override
			public String getSignature() {
				return ReflectionUIUtils.buildMethodSignature(this);
			}

			@Override
			public String getCaption() {
				return "Create " + getReturnValueType().getCaption();
			}

			@Override
			public ITypeInfo getReturnValueType() {
				return ValueAsListFieldInfo.this.getType();
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return new ArrayList(Arrays.asList(super.invoke(object, invocationData)));
			}
		};
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public String toString() {
		return "ValueAsListField [base=" + base + "]";
	}

	protected class ListTypeInfo extends StandardCollectionTypeInfo {

		public ListTypeInfo() {
			super(ValueAsListFieldInfo.this.reflectionUI, new JavaTypeInfoSource(ArrayList.class, null),
					ValueAsListFieldInfo.super.getType());
		}

		@Override
		public boolean isInsertionAllowed() {
			return false;
		}

		@Override
		public boolean isRemovalAllowed() {
			return false;
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

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object fromArray(Object[] array) {
			return new ArrayList(Arrays.asList(array));
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return ValueAsListFieldInfo.super.getType().getConstructors().stream()
					.map(ctor -> fromValueToListConstructor(ctor)).collect(Collectors.toList());
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this,
					new SpecificitiesIdentifier(objectType.getName(), ValueAsListFieldInfo.this.getName()));
		}

	}
}
