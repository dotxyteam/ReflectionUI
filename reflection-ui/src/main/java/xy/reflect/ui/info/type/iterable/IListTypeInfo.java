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

import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;

/**
 * This interface allows to specify UI-oriented properties of list types.
 * 
 * Generating UIs for lists is complex because there are multiple
 * implementations (arrays, collections, maps, ...) and not enough conventions.
 * A list can then be ordered or not, unmodifiable or not, supporting null items
 * or not, etc. This interface allows to describe all sorts of lists so that an
 * aware renderer will be able to display and allow as much as possible to edit
 * them all.
 * 
 * Structural preferences such as tabular or hierarchical facets of lists are
 * also supported via the {@link IListStructuralInfo} interface.
 * 
 * @author olitank
 *
 */
public interface IListTypeInfo extends ITypeInfo {

	/**
	 * @return the known type of items supported by this list type.
	 */
	ITypeInfo getItemType();

	/**
	 * @param listValue An instance of the current list type.
	 * @return the list of items in the given list value packed in a generic array.
	 */
	Object[] toArray(Object listValue);

	/**
	 * @return true if and only if this list type instances can be packed in a
	 *         generic array. Otherwise {@link #fromArray(Object[])} should not be
	 *         called.
	 */
	boolean canInstanciateFromArray();

	/**
	 * @param array A generic array containing items supported by this list type.
	 * @return a new instance of this list type containing the same items as the
	 *         generic array passed as parameter.
	 */
	Object fromArray(Object[] array);

	/**
	 * @return true if and only if an instance of this list type can have its list
	 *         of items replaced by calling
	 *         {@link #replaceContent(Object, Object[])}.
	 */
	boolean canReplaceContent();

	/**
	 * Replaces the list of items of the given instance by the one contained in the
	 * given generic array.
	 * 
	 * @param listValue An instance of the current list type.
	 * @param array     A generic array containing items supported by this list
	 *                  type.
	 */
	void replaceContent(Object listValue, Object[] array);

	/**
	 * @return tabular and hierarchical preferences of this list type.
	 */
	IListStructuralInfo getStructuralInfo();

	/**
	 * @return preferences about items display of this list type.
	 */
	IListItemDetailsAccessMode getDetailsAccessMode();

	/**
	 * @return whether the items in instances of this list type are ordered.
	 */
	boolean isOrdered();

	/**
	 * @return whether the item addition should be allowed on instances of this list
	 *         type.
	 */
	boolean isInsertionAllowed();

	/**
	 * @return whether the item removal should be allowed on instances of this list
	 *         type.
	 */
	boolean isRemovalAllowed();

	/**
	 * @return whether the item details display should be allowed on instances of
	 *         this list type.
	 */
	boolean canViewItemDetails();

	/**
	 * @param selection                       A list item position descriptors.
	 * @param listModificationFactoryAccessor An object that maps item positions to
	 *                                        list modification factories. This
	 *                                        object will usually be provided in
	 *                                        real-time by the renderer.
	 * @return actions that can be performed on a list instance according to a given
	 *         selection of items.
	 */
	List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	/**
	 * @param selection                       A list item position descriptors.
	 * @param listModificationFactoryAccessor An object that maps item positions to
	 *                                        list modification factories. This
	 *                                        object will usually be provided in
	 *                                        real-time by the renderer.
	 * @return properties of a list instance that can be accessed according to a
	 *         given selection of items.
	 */
	List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
			Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor);

	/**
	 * @return true if and only if instances of this list type supports null items.
	 */
	boolean isItemNullValueDistinct();

	/**
	 * @return an option describing how the UI reacts to item creation requests.
	 */
	InitialItemValueCreationOption getInitialItemValueCreationOption();

	/**
	 * @return the value return mode of the items of an instance of this list type.
	 *         It may impact the behavior of the list control.
	 */
	ValueReturnMode getItemReturnMode();

	/**
	 * Should be called by the renderer whenever the selection of items changes.
	 * 
	 * @param newSelection The new selection.
	 */
	void onSelection(List<? extends ItemPosition> newSelection);

	/**
	 * Allows to choose how the UI behaves when creating items. Typically it answers
	 * the question "should the framework require from users the creation option
	 * values or not ?".
	 * 
	 * @author olitank
	 *
	 */
	public enum InitialItemValueCreationOption {
		CREATE_INITIAL_NULL_VALUE, CREATE_INITIAL_VALUE_AUTOMATICALLY, CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES
	}

}
