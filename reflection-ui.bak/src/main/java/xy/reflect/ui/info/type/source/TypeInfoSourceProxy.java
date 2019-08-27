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
package xy.reflect.ui.info.type.source;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;

public class TypeInfoSourceProxy implements ITypeInfoSource {

	protected ITypeInfoSource base;

	public TypeInfoSourceProxy(ITypeInfoSource base) {
		super();
		this.base = base;
	}

	public ITypeInfo getTypeInfo(ReflectionUI reflectionUI) {
		return new InfoProxyFactory() {

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return TypeInfoSourceProxy.this;
			}

			@Override
			public String getIdentifier() {
				return TypeInfoSourceProxy.this.toString();
			}

		}.wrapTypeInfo(base.getTypeInfo(reflectionUI));
	}

	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return base.getSpecificitiesIdentifier();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		TypeInfoSourceProxy other = (TypeInfoSourceProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeInfoSourceProxy [base=" + base + "]";
	}

}
