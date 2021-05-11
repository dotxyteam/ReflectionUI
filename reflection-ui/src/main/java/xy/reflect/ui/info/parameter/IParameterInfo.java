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
package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify UI-oriented properties of method parameters.
 * 
 * @author olitank
 *
 */
public interface IParameterInfo extends IInfo {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	IParameterInfo NULL_PARAMETER_INFO = new IParameterInfo() {

		ITypeInfo type = new DefaultTypeInfo(ReflectionUIUtils.STANDARD_REFLECTION,
				new JavaTypeInfoSource(ReflectionUIUtils.STANDARD_REFLECTION, Object.class, null));

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return "NULL_PARAMETER_INFO";
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public ITypeInfo getType() {
			return type;
		}

		@Override
		public int getPosition() {
			return 0;
		}

		@Override
		public Object getDefaultValue(Object object) {
			return null;
		}

		@Override
		public boolean hasValueOptions(Object object) {
			return false;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	/**
	 * @return the type information of the current parameter.
	 */
	ITypeInfo getType();

	/**
	 * @return true if and only if this parameter control must distinctly display
	 *         and allow to set the null value. This is usually needed if a null
	 *         value has a special meaning different from "empty/default value" for
	 *         the developer.
	 */
	boolean isNullValueDistinct();

	/**
	 * @param object The object offering the method hosting this parameter.
	 * @return the default value of this parameter (may be null).
	 */
	Object getDefaultValue(Object object);

	/**
	 * @return the 0-based position of this parameter.
	 */
	int getPosition();

	/**
	 * @return true if and only if the parameter is filtered out from the display.
	 *         Then the parameter value is not required and the default value (may
	 *         be null) is used. It means that the default value is in a way
	 *         validated. Note that it does not alter the host method signature.
	 */
	boolean isHidden();

	/**
	 * @param object The object offering the method hosting this parameter.
	 * @return whether this parameter value has options.
	 */
	boolean hasValueOptions(Object object);

	/**
	 * @param object The object offering the method hosting this parameter.
	 * @return the options for the value of this parameter.
	 */
	Object[] getValueOptions(Object object);

}
