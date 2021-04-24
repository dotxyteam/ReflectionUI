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
package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;

/**
 * Allows to describe tabular and hierarchical preferences about list types.
 * 
 * @author olitank
 *
 */
public interface IListStructuralInfo {

	/**
	 * @return the list columns.
	 */
	List<IColumnInfo> getColumns();

	/**
	 * @param itemPosition The item position.
	 * @return the field used to get the sub-list value from the current item or
	 *         null if there is no sub-list.
	 */
	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	/**
	 * @param itemPosition The item position.
	 * @return the filter that must be applied to the item type information or null.
	 */
	IInfoFilter getItemInfoFilter(ItemPosition itemPosition);

	/**
	 * @return the height (in pixels) of the list or -1 if the default height should
	 *         be used.
	 */
	int getLength();

	/**
	 * Version of {@link MultipleFieldsAsListFieldInfo} used for list structure
	 * inspection only.
	 * 
	 * @author olitank
	 *
	 */
	public static class SubListsGroupingField extends MultipleFieldsAsListFieldInfo {

		public SubListsGroupingField(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingType) {
			super(reflectionUI, fields, containingType);
		}

		@Override
		public boolean isGetOnly() {
			return false;
		}

		@Override
		public void setValue(Object object, Object value) {
		}

		@Override
		protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
			return new SubListGroup(object, listFieldInfo);
		}

		@Override
		protected ITypeInfo getListItemTypeInfo(final IFieldInfo field) {
			return new SubListGroupTypeInfo(field);
		}

		public class SubListGroupTypeInfo extends ValueListItemTypeInfo {

			public SubListGroupTypeInfo(IFieldInfo field) {
				super(field);
			}

		}

		public class SubListGroup extends ValueListItem {

			public SubListGroup(Object object, IFieldInfo field) {
				super(object, field);
			}

		}

	}

}
