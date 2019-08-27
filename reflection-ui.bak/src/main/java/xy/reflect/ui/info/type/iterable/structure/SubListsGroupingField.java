/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class SubListsGroupingField extends MultipleFieldsAsListFieldInfo {

	public SubListsGroupingField(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingType) {
		super(reflectionUI, fields, containingType);
	}
	
	@Override
	protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new SubListGroup(object, listFieldInfo);
	}

	@Override
	protected ITypeInfo getListItemTypeInfo(final IFieldInfo field) {
		return new SubListGroupTypeInfo(field);
	}
	
	public class SubListGroupTypeInfo extends ValueListItemTypeInfo{

		public SubListGroupTypeInfo(IFieldInfo field) {
			super(field);
		}
		
	}

	public class SubListGroup extends ValueListItem{

		public SubListGroup(Object object, IFieldInfo field) {
			super(object, field);
		}
		
	}

}
