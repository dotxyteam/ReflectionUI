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
package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.ResourcePath;

/**
 * Base class of action menu item specifications.
 * 
 * @author olitank
 *
 */
public abstract class AbstractActionMenuItemInfo extends AbstractMenuItemInfo {

	protected ResourcePath iconImagePath;

	public AbstractActionMenuItemInfo(String name, ResourcePath iconImagePath) {
		super(name);
		this.iconImagePath = iconImagePath;
	}

	public AbstractActionMenuItemInfo() {
		super();
	}

	public ResourcePath getIconImagePath() {
		return iconImagePath;
	}

	public void setIconImagePath(ResourcePath iconImagePath) {
		this.iconImagePath = iconImagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((iconImagePath == null) ? 0 : iconImagePath.hashCode());
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
		AbstractActionMenuItemInfo other = (AbstractActionMenuItemInfo) obj;
		if (iconImagePath == null) {
			if (other.iconImagePath != null)
				return false;
		} else if (!iconImagePath.equals(other.iconImagePath))
			return false;
		return true;
	}

}
