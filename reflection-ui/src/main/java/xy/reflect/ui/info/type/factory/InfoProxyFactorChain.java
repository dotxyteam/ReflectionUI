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
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class InfoProxyFactorChain implements IInfoProxyFactory {

	protected List<IInfoProxyFactory> factories = new ArrayList<IInfoProxyFactory>();

	public InfoProxyFactorChain(IInfoProxyFactory... factories) {
		super();
		this.factories.addAll(Arrays.asList(factories));
	}

	@Override
	public ITypeInfo wrapTypeInfo(ITypeInfo type) {
		ITypeInfo result = type;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapTypeInfo(result);
		}
		return result;
	}

	@Override
	public IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo) {
		IApplicationInfo result = appInfo;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapApplicationInfo(result);
		}
		return result;
	}

}
