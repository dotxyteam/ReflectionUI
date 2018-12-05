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


public class Pair<T1,T2>{

	protected T1 first;
	protected T2 second;

	public Pair(T1 first, T2 second){
		if(first == null){
			throw new IllegalArgumentException("first == null");
		}
		if(second == null){
			throw new IllegalArgumentException("second == null");
		}
		this.first = first;
		this.second = second;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Pair)){
			return false;
		}
		@SuppressWarnings({ "rawtypes" })
		Pair other = (Pair) obj;
		if(! first.equals(other.first)){
			return false;
		}
		if(! second.equals(other.second)){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "first="+first+"\nsecond="+second;
	}
	
	
}
