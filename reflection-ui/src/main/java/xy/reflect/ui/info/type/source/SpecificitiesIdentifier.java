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

/**
 * This class allows to include a context in abstract UI type informations so
 * that appearance and behavior of the generated controls can be automatically
 * adjusted in specific contexts (in addition to global adjustments).
 * 
 * @author olitank
 *
 */
public class SpecificitiesIdentifier {

	protected String containingTypeName;
	protected String fieldName;

	public SpecificitiesIdentifier(String containingTypeName, String fieldName) {
		super();
		this.containingTypeName = containingTypeName;
		this.fieldName = fieldName;
	}

	public String getContainingTypeName() {
		return containingTypeName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingTypeName == null) ? 0 : containingTypeName.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		SpecificitiesIdentifier other = (SpecificitiesIdentifier) obj;
		if (containingTypeName == null) {
			if (other.containingTypeName != null)
				return false;
		} else if (!containingTypeName.equals(other.containingTypeName))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SpecificitiesIdentifier [containingTypeName=" + containingTypeName + ", fieldName=" + fieldName + "]";
	}

}
