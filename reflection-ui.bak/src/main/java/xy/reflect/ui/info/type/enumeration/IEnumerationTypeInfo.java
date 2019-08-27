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
package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify UI-oriented properties of enumeration types.
 * 
 * @author olitank
 *
 */
public interface IEnumerationTypeInfo extends ITypeInfo {

	/**
	 * @return the list of enumerated values.
	 */
	Object[] getPossibleValues();

	/**
	 * @param value
	 *            An item of the possible values of this type.
	 * @return a UI-oriented descriptor of the given item.
	 */
	IEnumerationItemInfo getValueInfo(Object value);

	/**
	 * @return true if and only if the possible values of this type are subject to
	 *         change. A false value would typically allow the renderer to perform
	 *         optimizations.
	 */
	boolean isDynamicEnumeration();
}
