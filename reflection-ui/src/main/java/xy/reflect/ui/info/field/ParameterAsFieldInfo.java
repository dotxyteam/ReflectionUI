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
package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual field that allows to view/edit the underlying method parameter value.
 * 
 * @author olitank
 *
 */
public class ParameterAsFieldInfo extends VirtualFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IParameterInfo param;
	protected IMethodInfo method;
	protected ITypeInfo containingType;

	protected ITypeInfo type;

	public ParameterAsFieldInfo(ReflectionUI reflectionUI, IMethodInfo method, IParameterInfo param,
			ITypeInfo containingType) {
		super(param.getName(), param.getType());
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
		this.method = method;
		this.param = param;
	}

	public static String buildParameterFieldName(String baseMethodSignature, String parameterName) {
		return parameterName + "Of-" + ReflectionUIUtils.buildNameFromMethodSignature(baseMethodSignature);
	}

	public static String buildLegacyParameterFieldName(String baseMethodName, String parameterName) {
		return baseMethodName + "." + parameterName;
	}

	@Override
	public String getName() {
		return buildParameterFieldName(method.getSignature(), param.getName());
	}

	@Override
	public String getCaption() {
		return param.getCaption();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(param.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(containingType.getName(), getName());
				}
			});
		}
		return type;
	}

	public void ensureInitialValueIsDefaultParameterValue(Object object) {
		if (!isInitialized(object)) {
			getValueByField(object).put(this, param.getDefaultValue(object));
		}
	}

	public boolean isInitialized(Object object) {
		return getValueByField(object).containsKey(this);
	}

	@Override
	public Object getValue(Object object) {
		ensureInitialValueIsDefaultParameterValue(object);
		return super.getValue(object);
	}

	@Override
	public boolean isNullValueDistinct() {
		return param.isNullValueDistinct();
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return param.hasValueOptions(object);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return param.getValueOptions(object);
	}

	@Override
	public String getOnlineHelp() {
		return param.getOnlineHelp();
	}

	@Override
	public String toString() {
		return "ParameterAsFieldInfo [method=" + method + ", param=" + param + "]";
	}

}
