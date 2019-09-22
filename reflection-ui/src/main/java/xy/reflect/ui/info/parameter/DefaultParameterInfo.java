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

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultParameterInfo extends AbstractInfo implements IParameterInfo {

	protected ReflectionUI reflectionUI;
	protected Parameter javaParameter;
	protected ITypeInfo type;
	protected String name;

	public static boolean isCompatibleWith(Parameter javaParameter) {
		return true;
	}

	public DefaultParameterInfo(ReflectionUI reflectionUI, Parameter javaParameter) {
		this.reflectionUI = reflectionUI;
		this.javaParameter = javaParameter;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultParameterCaption(this);
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaParameter.getType(),
					javaParameter.getDeclaringInvokable(), javaParameter.getPosition(), null));
		}
		return type;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = javaParameter.getName();
			if (name == Parameter.NO_NAME) {
				name = new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(javaParameter.getType(), null))
						.getCaption();
				int sameNameCount = 0;
				int sameNamePosition = 0;
				int parameterposition = 0;
				for (Class<?> c : javaParameter.getDeclaringInvokableParameterTypes()) {
					if (name.equals(new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(c, null)).getCaption())) {
						sameNameCount++;
						if (parameterposition < javaParameter.getPosition()) {
							sameNamePosition++;
						}
					}
					parameterposition++;
				}
				if (sameNameCount > 1) {
					name += (sameNamePosition + 1);
				}
				name = name.replace(" ", "");
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
			}
		}
		return name;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public Object getDefaultValue(Object object) {
		if (javaParameter.getType().isPrimitive()) {
			return ClassUtils.getDefaultPrimitiveValue(javaParameter.getType());
		} else {
			return null;
		}
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public int getPosition() {
		return javaParameter.getPosition();
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaParameter == null) ? 0 : javaParameter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultParameterInfo other = (DefaultParameterInfo) obj;
		if (javaParameter == null) {
			if (other.javaParameter != null)
				return false;
		} else if (!javaParameter.equals(other.javaParameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultParameterInfo [javaParameter=" + javaParameter + "]";
	}
}
