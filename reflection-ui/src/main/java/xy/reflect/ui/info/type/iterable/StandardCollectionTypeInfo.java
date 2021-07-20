


package xy.reflect.ui.info.type.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Type information extracted from the Java collection type encapsulated in the
 * given type information source.
 * 
 * @author olitank
 *
 */
public class StandardCollectionTypeInfo extends DefaultTypeInfo implements IListTypeInfo {

	protected ITypeInfo itemType;

	public StandardCollectionTypeInfo(JavaTypeInfoSource source, ITypeInfo itemType) {
		super(source);
		this.itemType = itemType;
	}

	@Override
	public ITypeInfo getItemType() {
		return itemType;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultListTypeCaption(this);
	}

	@Override
	public boolean isItemNullValueSupported() {
		return false;
	}

	@Override
	public InitialItemValueCreationOption getInitialItemValueCreationOption() {
		return InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES;
	}

	@Override
	public ValueReturnMode getItemReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Collection tmpCollection = new ArrayList();
		for (Object item : array) {
			if (listValue instanceof Set) {
				if (tmpCollection.contains(item)) {
					throw new ReflectionUIError(
							"Duplicate item: '" + ReflectionUIUtils.toString(reflectionUI, item) + "'");
				}
			}
			tmpCollection.add(item);
		}
		Collection collection = (Collection) listValue;
		collection.clear();
		collection.addAll(tmpCollection);
	}

	@Override
	public boolean canInstanciateFromArray() {
		return ReflectionUIUtils.canCreateDefaultInstance(this, false) && canReplaceContent();
	}

	@Override
	public Object fromArray(Object[] array) {
		Object result = ReflectionUIUtils.createDefaultInstance(this, false);
		replaceContent(result, array);
		return result;
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
		if (List.class.isAssignableFrom(getJavaType())) {
			return true;
		}
		if (LinkedHashSet.class.isAssignableFrom(getJavaType())) {
			return true;
		}
		return false;
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
	public List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return Collections.emptyList();
	}

	@Override
	public List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		return Collections.emptyList();
	}

	@Override
	public boolean canCopy(Object object) {
		if (super.canCopy(object)) {
			return true;
		}
		Object[] srcArray = toArray(object);
		if (canInstanciateFromArray()) {
			boolean ok = true;
			for (Object srcItem : srcArray) {
				ITypeInfo itemType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(srcItem));
				if (!itemType.isImmutable()) {
					if (!ReflectionUIUtils.canCopy(reflectionUI, srcItem)) {
						ok = false;
						break;
					}
				}
			}
			return ok;
		} else if (canReplaceContent()) {
			return ReflectionUIUtils.canCreateDefaultInstance(this, false);
		} else {
			return false;
		}
	}

	@Override
	public Object copy(Object object) {
		if (super.canCopy(object)) {
			return super.copy(object);
		}
		Object[] srcArray = toArray(object);
		if (canInstanciateFromArray()) {
			Object[] dstArray = new Object[srcArray.length];
			int i = 0;
			for (Object srcItem : srcArray) {
				ITypeInfo itemType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(srcItem));
				if (itemType.isImmutable()) {
					dstArray[i] = srcItem;
				} else {
					Object dstItem = ReflectionUIUtils.copy(reflectionUI, srcItem);
					dstArray[i] = dstItem;
				}
				i++;
			}
			return fromArray(dstArray);
		} else if (canReplaceContent()) {
			Object newInstance = ReflectionUIUtils.createDefaultInstance(this, false);
			replaceContent(newInstance, srcArray);
			return newInstance;
		} else {
			throw new ReflectionUIError("Cannot copy list: '" + object + "'");
		}
	}

	@Override
	public String toString(Object object) {
		List<String> result = new ArrayList<String>();
		for (Object item : toArray(object)) {
			result.add(ReflectionUIUtils.toString(reflectionUI, item));
		}
		return MiscUtils.stringJoin(result, ", ");
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
		return "StandardCollectionTypeInfo [source=" + source + ", itemType=" + itemType + "]";
	}

}
