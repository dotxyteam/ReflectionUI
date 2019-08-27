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
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class FilteredTypeFactory extends InfoProxyFactory {

	protected IInfoFilter infoFilter;

	public FilteredTypeFactory(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	@Override
	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : super.getFields(type)) {
			if (infoFilter.excludeField(field)) {
				continue;
			}
			result.add(field);
		}
		return result;
	}

	@Override
	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : super.getMethods(type)) {
			if (infoFilter.excludeMethod(method)) {
				continue;
			}
			result.add(method);
		}
		return result;
	}

	@Override
	public String toString() {
		return "FilteredTypeFactory [infoFilter=" + infoFilter + "]";
	}
	
	

}
