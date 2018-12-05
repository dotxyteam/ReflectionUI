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
package xy.reflect.ui.info.menu;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class represents a menu item that will be used to execute a given
 * method.
 * 
 * @author olitank
 *
 */
public class MethodActionMenuItem extends AbstractActionMenuItem {

	protected IMethodInfo method;

	public MethodActionMenuItem(ReflectionUI reflectionUI, IMethodInfo method) {
		super(ReflectionUIUtils.formatMethodControlCaption(method.getCaption(), method.getParameters()),
				method.getIconImagePath());
		this.method = method;
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public void setMethod(IMethodInfo method) {
		this.method = method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodActionMenuItem other = (MethodActionMenuItem) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActionMenuItem [name=" + name + ", action=" + method + "]";
	}

}
