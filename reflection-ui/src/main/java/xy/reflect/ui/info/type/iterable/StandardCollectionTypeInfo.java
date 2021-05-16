/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.type.iterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
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

	public StandardCollectionTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source, ITypeInfo itemType) {
		super(reflectionUI, source);
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
	public void onSelection(List<? extends ItemPosition> newSelection) {
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
		return isConcrete() && (ReflectionUIUtils.getZeroParameterMethod(getConstructors()) != null)
				&& canReplaceContent();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object fromArray(Object[] array) {
		IMethodInfo constructor = ReflectionUIUtils.getZeroParameterMethod(getConstructors());
		Collection result = (Collection) constructor.invoke(null, new InvocationData(null, constructor));
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
		if (Set.class.equals(getJavaType())) {
			return false;
		}
		if (HashSet.class.equals(getJavaType())) {
			return false;
		}
		if (SortedSet.class.isAssignableFrom(getJavaType())) {
			return false;
		}
		return true;
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
