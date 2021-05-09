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
package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify a plugin that will provide a selectable
 * custom field control.
 * 
 * @author olitank
 *
 */
public interface IFieldControlPlugin {

	/**
	 * The key that references the name of the chosen field control plugin in the
	 * map returned by {@link ITypeInfo#getSpecificProperties()}.
	 */
	public String CHOSEN_PROPERTY_KEY = IFieldControlPlugin.class.getName() + ".CHOSEN";

	/**
	 * @param input The specification of the field to be displayed.
	 * @return whether the field described by the given input object can be
	 *         displayed by the current plugin control.
	 */
	boolean handles(IFieldControlInput input);

	/**
	 * @return whether the current plugin control can handle (display distinctly and
	 *         allow to set) the null value.
	 */
	boolean canDisplayDistinctNullValue();

	/**
	 * @param renderer The UI renderer object.
	 * @param input    The specification of the field to be displayed.
	 * @return A control able to display the field specified by the given input
	 *         object.
	 * @throws RejectedFieldControlInputException If it is discovered during the
	 *                                            creation that the input cannot be
	 *                                            handled.
	 */
	Object createControl(Object renderer, IFieldControlInput input) throws RejectedFieldControlInputException;

	/**
	 * @return words describing the current plugin.
	 */
	String getControlTitle();

	/**
	 * @return a string that uniquely identifies the current plugin.
	 */
	String getIdentifier();

	/**
	 * @param renderer    The UI renderer object.
	 * @param controlData An object allowing to handle the value of the field to be
	 *                    displayed.
	 * @return an output field control data corresponding to the input field control
	 *         data but optionally modified to handle conveniently null values.
	 *         Typically its type would have an additional zero-arg constructor
	 *         allowing to move smoothly from a null to a non-null field value. This
	 *         method is not used if {@link #canDisplayDistinctNullValue()} returns
	 *         true.
	 */
	IFieldControlData filterDistinctNullValueControlData(Object renderer, IFieldControlData controlData);
}
