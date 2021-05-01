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

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;

/**
 * Allows to describe tabular and hierarchical preferences about list types.
 * 
 * @author olitank
 *
 */
public interface IListStructuralInfo {

	/**
	 * @return the list of columns.
	 */
	List<IColumnInfo> getColumns();

	/**
	 * @param itemPosition The position of the item in the list/tree.
	 * @return the field used to get the sub-list value from the current item or
	 *         null if there is no sub-list.
	 */
	IFieldInfo getItemSubListField(ItemPosition itemPosition);

	/**
	 * @param itemPosition The position of the item in the list/tree.
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
	 * creation and inspection only.
	 * 
	 * @author olitank
	 *
	 */
	public static class SubListGroupField extends MultipleFieldsAsListFieldInfo {

		public SubListGroupField(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingItemType) {
			super(reflectionUI, fields, containingItemType);
		}

		@Override
		public String getName() {
			return "subListGroup [containingItemType=" + getContainingItemType().getName() + "]";
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
			return new SubListGroupItem(object, listFieldInfo);
		}

		@Override
		public ITypeInfo getType() {
			if (type == null) {
				type = reflectionUI.getTypeInfo(new SubListGroupTypeInfo().getSource());
			}
			return type;
		}

		@Override
		public Object getValue(Object object) {
			List<ValueListItem> result = new ArrayList<ValueListItem>();
			for (IFieldInfo field : fields) {
				ValueListItem listItem = getListItem(object, field);
				reflectionUI.registerPrecomputedTypeInfoObject(listItem, new SubListGroupItemTypeInfo(field));
				result.add(listItem);
			}
			return result;
		}

		public ITypeInfo getContainingItemType() {
			return containingType;
		}

		@Override
		public String toString() {
			return "SubListGroupField [containingItemType=" + getContainingItemType() + "]";
		}

		public class SubListGroupTypeInfo extends ValueListTypeInfo {

			@Override
			public IListStructuralInfo getStructuralInfo() {
				return new ListStructuralInfoProxy(super.getStructuralInfo()) {

					@Override
					public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
						return new SubListGroupItemDetailsFieldInfo(
								((SubListGroupItem) itemPosition.getItem()).getField());
					}

				};
			}

			@Override
			public String toString() {
				return "SubListGroupTypeInfo [containingItemType=" + getContainingItemType() + "]";
			}

		}

		public class SubListGroupItemTypeInfo extends ValueListItemTypeInfo {

			public SubListGroupItemTypeInfo(IFieldInfo field) {
				super(field);
			}

			@Override
			public String getName() {
				return "SubListGroupItemTypeInfo [of=" + SubListGroupField.this.getName() + ", itemField="
						+ field.getName() + "]";
			}

			@Override
			public IFieldInfo getDetailsField() {
				return new SubListGroupItemDetailsFieldInfo(field);
			}

			@Override
			public String toString() {
				return "SubListGroupItemTypeInfo [of=" + SubListGroupField.this + ", itemField=" + field + "]";
			}

		}

		public class SubListGroupItem extends ValueListItem {

			public SubListGroupItem(Object object, IFieldInfo field) {
				super(object, field);
			}

		}

		public class SubListGroupItemDetailsFieldInfo extends ValueListItemDetailsFieldInfo {

			public SubListGroupItemDetailsFieldInfo(IFieldInfo field) {
				super(field);
			}

			public SubListGroupItemTypeInfo getSubListGroupItemTypeInfo() {
				return new SubListGroupItemTypeInfo(base);
			}

			@Override
			public ITypeInfo getType() {
				return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(super.getType().getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return new SpecificitiesIdentifier(
								new SubListGroupItemTypeInfo(SubListGroupItemDetailsFieldInfo.this.base).getName(),
								SubListGroupItemDetailsFieldInfo.this.getName());
					}
				});
			}

			@Override
			public String toString() {
				return "SubListGroupItemDetailsFieldInfo [of=" + SubListGroupField.this + ", itemField=" + base + "]";
			}
		}

	}

}
