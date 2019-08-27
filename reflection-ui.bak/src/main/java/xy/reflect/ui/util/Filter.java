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

public interface Filter<T> {

	T get(T t);

	public static class Chain<T> implements Filter<T> {

		protected Filter<T> filter1;
		protected Filter<T> filter2;

		public Chain(Filter<T> filter1, Filter<T> filter2) {
			this.filter1 = filter1;
			this.filter2 = filter2;
		}

		@Override
		public T get(T t) {
			T t1 = filter1.get(t);
			T t2 = filter2.get(t1);
			return t2;
		}

	}

}
