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
package xy.reflect.ui.util;

import java.util.Arrays;

public class ArrayAccessor<T> extends Accessor<T> {

	protected T[] arrayValue;

	public ArrayAccessor(T[] arrayValue) {
		super();
		this.arrayValue = arrayValue;
	}

	@Override
	public T get() {
		return arrayValue[0];
	}

	@Override
	public void set(T t) {
		arrayValue[0] = t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arrayValue);
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
		ArrayAccessor<?> other = (ArrayAccessor<?>) obj;
		if (!Arrays.equals(arrayValue, other.arrayValue))
			return false;
		return true;
	}

}
