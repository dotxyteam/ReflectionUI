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
package xy.reflect.ui.util;

/**
 * Simple generic filter interface.
 * 
 * @author olitank
 *
 * @param <T> The type that is filtered.
 */
public interface Filter<T> {

	public static <T> Filter<T> nullFilter() {
		return new Filter<T>() {
			@Override
			public T get(T t) {
				return t;
			}
		};
	}

	/**
	 * @param t The value.
	 * @return the filtered version of the given value.
	 */
	T get(T t);

	/**
	 * Simple generic filter chain.
	 * 
	 * @author olitank
	 *
	 * @param <T> The type that is filtered.
	 */
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((filter1 == null) ? 0 : filter1.hashCode());
			result = prime * result + ((filter2 == null) ? 0 : filter2.hashCode());
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
			@SuppressWarnings("rawtypes")
			Chain other = (Chain) obj;
			if (filter1 == null) {
				if (other.filter1 != null)
					return false;
			} else if (!filter1.equals(other.filter1))
				return false;
			if (filter2 == null) {
				if (other.filter2 != null)
					return false;
			} else if (!filter2.equals(other.filter2))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FilterChain [filter1=" + filter1 + ", filter2=" + filter2 + "]";
		}

		
		
	}

}
