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
package xy.reflect.ui.info.type.iterable.structure.column;

import java.util.Map;

import xy.reflect.ui.info.AbstractInfoProxy;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

public class ColumnInfoProxy extends AbstractInfoProxy implements IColumnInfo {
	protected IColumnInfo base;

	public ColumnInfoProxy(IColumnInfo base) {
		super();
		this.base = base;
	}

	public String getCaption() {
		return base.getCaption();
	}

	public boolean hasCellValue(ItemPosition itemPosition) {
		return base.hasCellValue(itemPosition);
	}

	public String getCellValue(ItemPosition itemPosition) {
		return base.getCellValue(itemPosition);
	}

	public int getMinimalCharacterCount() {
		return base.getMinimalCharacterCount();
	}

	public String getName() {
		return base.getName();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		ColumnInfoProxy other = (ColumnInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ColumnInfoProxy [base=" + base + "]";
	}

}
