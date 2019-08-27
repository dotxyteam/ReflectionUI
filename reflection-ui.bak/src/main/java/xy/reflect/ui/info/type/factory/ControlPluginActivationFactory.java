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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ControlPluginActivationFactory extends InfoProxyFactory {

	protected String pluginId;
	protected Serializable pluginConfiguration;

	public ControlPluginActivationFactory(String pluginId, Serializable pluginConfiguration) {
		this.pluginId = pluginId;
		this.pluginConfiguration = pluginConfiguration;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
		ReflectionUIUtils.setFieldControlPluginIdentifier(result, pluginId);
		ReflectionUIUtils.setFieldControlPluginConfiguration(result, pluginId, pluginConfiguration);
		return result;
	}

}
