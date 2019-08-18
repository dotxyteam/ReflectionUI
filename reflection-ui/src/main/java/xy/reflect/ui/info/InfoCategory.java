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
package xy.reflect.ui.info;

/**
 * This class is used to categorize fields and methods of an abstract UI model.
 * Note that a category is identified by both its name and position.
 * 
 * @author olitank
 *
 */
public class InfoCategory implements Comparable<InfoCategory> {

	protected String caption;
	protected int position;
	protected ResourcePath iconImagePath;

	public InfoCategory(String caption, int position, ResourcePath iconImagePath) {
		super();
		this.caption = caption;
		this.position = position;
		this.iconImagePath = iconImagePath;
	}

	public String getCaption() {
		return caption;
	}

	public int getPosition() {
		return position;
	}

	public ResourcePath getIconImagePath() {
		return iconImagePath;
	}

	@Override
	public int compareTo(InfoCategory o) {
		int result = new Integer(position).compareTo(o.position);
		if (result == 0) {
			result = caption.compareTo(o.caption);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((caption == null) ? 0 : caption.hashCode());
		result = prime * result + ((iconImagePath == null) ? 0 : iconImagePath.hashCode());
		result = prime * result + position;
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
		InfoCategory other = (InfoCategory) obj;
		if (caption == null) {
			if (other.caption != null)
				return false;
		} else if (!caption.equals(other.caption))
			return false;
		if (iconImagePath == null) {
			if (other.iconImagePath != null)
				return false;
		} else if (!iconImagePath.equals(other.iconImagePath))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InfoCategory [caption=" + caption + ", position=" + position + ", iconImagePath=" + iconImagePath + "]";
	}

}
