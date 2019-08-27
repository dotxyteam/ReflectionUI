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
package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

public class NerverNullFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containgType;
	protected ITypeInfo type;

	protected static final Object NULL_REPLACEMENT = new Object() {
		@Override
		public String toString() {
			return "NULL_REPLACEMENT";
		}
	};

	public NerverNullFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo containgType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.containgType = containgType;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = NULL_REPLACEMENT;
		}
		return result;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class,
					new SpecificitiesIdentifier(containgType.getName(), getName())));
		}
		return type;
	}

	@Override
	public String toString() {
		return "NerverNullField []";
	}

}
