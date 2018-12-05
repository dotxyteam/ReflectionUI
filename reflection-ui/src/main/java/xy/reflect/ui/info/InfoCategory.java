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

	public InfoCategory(String caption, int position) {
		this.caption = caption;
		this.position = position;
	}

	public String getCaption() {
		return caption;
	}

	public int getPosition() {
		return position;
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
		return position + caption.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InfoCategory)) {
			return false;
		}
		InfoCategory other = (InfoCategory) obj;
		if (position != other.position) {
			return false;
		}
		if (!caption.equals(other.caption)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return InfoCategory.class.getSimpleName() + " n°" + (position + 1) + " - " + caption;
	}

}
