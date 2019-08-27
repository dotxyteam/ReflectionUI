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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;

public class StandardEnumerationTypeInfo extends DefaultTypeInfo implements IEnumerationTypeInfo {

	public StandardEnumerationTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		super(reflectionUI, source);
	}

	@Override
	public boolean isDynamicEnumeration() {
		return false;
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public Object[] getPossibleValues() {
		return getJavaType().getEnumConstants();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			ITypeInfo returnValueType;

			@Override
			public ITypeInfo getReturnValueType() {
				if (returnValueType == null) {
					returnValueType = reflectionUI
							.getTypeInfo(new PrecomputedTypeInfoSource(StandardEnumerationTypeInfo.this, null));
				}
				return returnValueType;
			}

			@Override
			public Object invoke(Object parentObject, InvocationData invocationData) {
				return getJavaType().getEnumConstants()[0];
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		});
	}

	@Override
	public IEnumerationItemInfo getValueInfo(final Object object) {
		if (object == null) {
			return null;
		} else {
			return new IEnumerationItemInfo() {

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ResourcePath getIconImagePath() {
					return null;
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public String getName() {
					return object.toString();
				}

				@Override
				public Object getItem() {
					return object;
				}

				@Override
				public String getCaption() {
					return object.toString();
				}

				@Override
				public String toString() {
					return object.toString();
				}
			};
		}
	}

	@Override
	public String toString() {
		return "StandardEnumerationTypeInfo [source=" + getSource() + "]";
	}

}
