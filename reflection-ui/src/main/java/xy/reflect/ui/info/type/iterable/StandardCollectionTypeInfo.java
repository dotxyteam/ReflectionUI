package xy.reflect.ui.info.type.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class StandardCollectionTypeInfo extends DefaultTypeInfo implements IListTypeInfo {

	protected ITypeInfo itemType;

	public StandardCollectionTypeInfo(ReflectionUI reflectionUI, Class<?> javaType, ITypeInfo itemType) {
		super(reflectionUI, javaType);
		this.itemType = itemType;
	}

	@Override
	public ITypeInfo getItemType() {
		return itemType;
	}

	@Override
	public String getCaption() {
		if (itemType == null) {
			return "List";
		} else {
			return "List of " + getItemType().getCaption() + " elements";
		}
	}

	public Class<?> getJavaType() {
		return javaType;
	}


	@Override
	public List<IMethodInfo> getConstructors() {
		if (ReflectionUIUtils.getZeroParameterMethod(super.getConstructors()) != null) {
			return super.getConstructors();
		} else {
			IMethodInfo zeroParameterCtor = createZeroParameterContructor();
			if (zeroParameterCtor != null) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors());
				result.add(zeroParameterCtor);
				return result;
			} else {
				return super.getConstructors();
			}
		}
	}

	protected IMethodInfo createZeroParameterContructor() {
		final Collection<?> newInstance;
		if (javaType.isAssignableFrom(ArrayList.class)) {
			newInstance = new ArrayList<Object>();
		} else if (javaType.isAssignableFrom(HashSet.class)) {
			newInstance = new HashSet<Object>();
		} else {
			newInstance = null;
		}
		if (newInstance != null) {
			return new AbstractConstructorInfo(this) {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					return newInstance;
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}
			};
		}
		return null;
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Collection collection = (Collection) listValue;
		collection.clear();
		for (Object item : array) {
			collection.add(item);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromArray(Object[] array) {
		IMethodInfo constructor = ReflectionUIUtils.getZeroParameterConstrucor(this);
		Collection result = (Collection) constructor.invoke(null, new InvocationData());
		for (Object item : array) {
			if (result instanceof Set) {
				if (result.contains(item)) {
					throw new ReflectionUIError(
							"Duplicate item: '" + ReflectionUIUtils.toString(reflectionUI, item) + "'");
				}
			}
			result.add(item);
		}
		return result;
	}

	@Override
	public boolean canInstanciateFromArray() {
		return ReflectionUIUtils.getZeroParameterConstrucor(this) != null;
	}

	@Override
	public Object[] toArray(Object listValue) {
		return ((Collection<?>) listValue).toArray();
	}

	@Override
	public IListStructuralInfo getStructuralInfo() {
		return new DefaultListStructuralInfo(reflectionUI);
	}

	@Override
	public IListItemDetailsAccessMode getDetailsAccessMode() {
		return new DetachedItemDetailsAccessMode();
	}

	@Override
	public boolean isOrdered() {
		return List.class.isAssignableFrom(javaType);
	}

	@Override
	public boolean canAdd() {
		return true;
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canViewItemDetails() {
		return true;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Collection.class.isAssignableFrom(javaType)) {
			return true;
		}
		return false;
	}

	@Override
	public List<AbstractListAction> getDynamicActions(List<? extends ItemPosition> selection) {
		return Collections.emptyList();
	}

	@Override
	public List<AbstractListProperty> getDynamicProperties(List<? extends ItemPosition> selection) {
		return Collections.emptyList();
	}

	@Override
	public List<IMethodInfo> getAdditionalItemConstructors(Object listValue) {
		return Collections.emptyList();
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardCollectionTypeInfo other = (StandardCollectionTypeInfo) obj;
		if (itemType == null) {
			if (other.itemType != null)
				return false;
		} else if (!itemType.equals(other.itemType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardCollectionTypeInfo [listType=" + javaType + ", itemType=" + itemType + "]";
	}

}
