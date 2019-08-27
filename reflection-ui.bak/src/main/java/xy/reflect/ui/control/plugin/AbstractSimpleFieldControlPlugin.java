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
package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.util.ClassUtils;

public abstract class AbstractSimpleFieldControlPlugin implements IFieldControlPlugin {

	protected abstract boolean handles(Class<?> javaType);

	@Override
	public String getIdentifier() {
		return getClass().getName();
	}

	@Override
	public boolean handles(IFieldControlInput input) {
		final Class<?> javaType;
		try {
			javaType = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (!handles(javaType)) {
			return false;
		}
		return true;
	}

	@Override
	public IFieldControlData filterDistinctNullValueControlData(Object renderer, IFieldControlData controlData) {
		return controlData;
	}

}
