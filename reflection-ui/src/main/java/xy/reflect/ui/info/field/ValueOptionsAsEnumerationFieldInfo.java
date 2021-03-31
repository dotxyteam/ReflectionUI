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

import java.util.Arrays;
import java.util.Iterator;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

public class ValueOptionsAsEnumerationFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;

	protected Object object;
	protected ITypeInfo objectType;
	protected GenericEnumerationFactory enumFactory;
	protected ITypeInfo enumType;

	public ValueOptionsAsEnumerationFieldInfo(ReflectionUI reflectionUI, Object object, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		this.enumFactory = createEnumerationFactory();
		this.enumType = reflectionUI.getTypeInfo(
				enumFactory.getInstanceTypeInfoSource(new SpecificitiesIdentifier(objectType.getName(), getName())));
	}

	protected GenericEnumerationFactory createEnumerationFactory() {
		String enumTypeName = "ValueOptions [ownerType=" + objectType.getName() + ", field=" + base.getName() + "]";
		Iterable<Object> iterable = new Iterable<Object>() {
			@Override
			public Iterator<Object> iterator() {
				Object[] valueOptions = base.getValueOptions(object);
				return Arrays.asList(valueOptions).iterator();
			}
		};
		return new GenericEnumerationFactory(reflectionUI, iterable, enumTypeName, "", false);
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
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		return enumFactory.getInstance(value);
	}

	@Override
	public void setValue(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		super.setValue(object, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		return super.getNextUpdateCustomUndoJob(object, value);
	}

	@Override
	public ITypeInfo getType() {
		return enumType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueOptionsAsEnumerationFieldInfo other = (ValueOptionsAsEnumerationFieldInfo) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}

}
