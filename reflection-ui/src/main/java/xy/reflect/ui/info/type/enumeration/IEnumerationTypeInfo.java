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
package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify UI-oriented properties of enumeration types.
 * Note that values are distinct from item information instances that actually
 * enrich the values.
 * 
 * @author olitank
 *
 */
public interface IEnumerationTypeInfo extends ITypeInfo {

	/**
	 * @return the list of enumerated items. Note that there may be other values
	 *         (not in this list) that are supported by this type
	 *         ({@link #supports(Object)} returns true).
	 */
	Object[] getValues();

	/**
	 * @param value A possible value of this type.
	 * @return the enumeration item information associated with the given value.
	 */
	IEnumerationItemInfo getValueInfo(Object value);

	/**
	 * @return true if and only if the possible values of this type are subject to
	 *         change. A false return value would typically allow the renderer to
	 *         perform optimizations.
	 */
	boolean isDynamicEnumeration();
}
