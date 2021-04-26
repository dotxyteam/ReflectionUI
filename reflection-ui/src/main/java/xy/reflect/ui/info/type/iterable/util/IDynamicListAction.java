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
package xy.reflect.ui.info.type.iterable.util;

import java.util.List;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * This class allows to specify an action that can be performed on a list
 * instance according to the current selection of items. Such an action will
 * typically be available on the list control tool bar.
 * 
 * The current selection is provided through
 * {@link IListTypeInfo#getDynamicActions(List, xy.reflect.ui.util.Mapper)}.
 * 
 * Note that the owner object passed to
 * {@link #invoke(Object, xy.reflect.ui.info.method.InvocationData)} is
 * {@link IDynamicListAction#NO_OWNER}.
 * 
 * @author olitank
 *
 */
public interface IDynamicListAction extends IMethodInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return AbstractListProperty.class.getName() + ".NO_OWNER";
		}

	};

	/**
	 * @return the list of item positions that should be selected after the
	 *         execution of the current action or null if the selection should not
	 *         be updated.
	 */
	List<ItemPosition> getPostSelection();

	/**
	 * @return whether the list action can be executed or not.
	 */
	boolean isEnabled();

}
