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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Base class of simple field control plugins that can be configured.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSimpleCustomizableFieldControlPlugin extends AbstractSimpleFieldControlPlugin
		implements ICustomizableFieldControlPlugin {

	@Override
	public Map<String, Object> storeControlCustomization(AbstractConfiguration controlConfiguration,
			Map<String, Object> specificProperties) {
		specificProperties = new HashMap<String, Object>(specificProperties);
		ReflectionUIUtils.setFieldControlPluginConfiguration(specificProperties, getIdentifier(), controlConfiguration);
		return specificProperties;
	}

	@Override
	public AbstractConfiguration getControlCustomization(Map<String, Object> specificProperties) {
		AbstractConfiguration result = loadControlCustomization(specificProperties);
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public AbstractConfiguration loadControlCustomization(Map<String, Object> specificProperties) {
		return (AbstractConfiguration) ReflectionUIUtils.getFieldControlPluginConfiguration(specificProperties,
				getIdentifier());
	}

	public AbstractConfiguration loadControlCustomization(IFieldControlInput input) {
		AbstractConfiguration result = loadControlCustomization(
				input.getControlData().getType().getSpecificProperties());
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public static abstract class AbstractConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int hashCode() {
			return Arrays.hashCode(IOUtils.serializeToBinary(this));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			if (!Arrays.equals(IOUtils.serializeToBinary(obj), IOUtils.serializeToBinary(this)))
				return false;
			return true;
		}

	}

}
