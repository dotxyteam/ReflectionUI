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
package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CompositeModification implements IModification {

	protected IModification[] modifications;
	protected String title;
	protected UndoOrder undoOrder;

	public CompositeModification(String title, UndoOrder undoOrder, IModification... modifications) {
		this.title = title;
		this.undoOrder = undoOrder;
		this.modifications = modifications;
	}

	public CompositeModification(String title, UndoOrder undoOrder, List<IModification> modifications) {
		this(title, undoOrder, modifications.toArray(new IModification[modifications.size()]));
	}

	@Override
	public boolean isNull() {
		for (IModification modif : modifications) {
			if (!modif.isNull()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isFake() {
		for (IModification modif : modifications) {
			if (!modif.isFake()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IModification applyAndGetOpposite() {
		List<IModification> oppositeModifications = new ArrayList<IModification>();
		for (IModification modif : modifications) {
			if (undoOrder == UndoOrder.getNormal()) {
				oppositeModifications.add(0, modif.applyAndGetOpposite());
			} else if (undoOrder == UndoOrder.getInverse()) {
				oppositeModifications.add(modif.applyAndGetOpposite());
			} else {
				throw new ReflectionUIError();
			}
		}
		return new CompositeModification(AbstractModification.getUndoTitle(title), undoOrder, oppositeModifications);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		if (title != null) {
			return title;
		} else {
			List<String> result = new ArrayList<String>();
			for (IModification modif : modifications) {
				result.add(modif.getTitle());
			}
			return ReflectionUIUtils.stringJoin(result, ", ");
		}
	}

	public IModification[] getModifications() {
		return modifications;
	}

	public void setModifications(IModification[] modifications) {
		this.modifications = modifications;
	}

	public UndoOrder getUndoOrder() {
		return undoOrder;
	}

	public void setUndoOrder(UndoOrder undoOrder) {
		this.undoOrder = undoOrder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(modifications);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((undoOrder == null) ? 0 : undoOrder.hashCode());
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
		CompositeModification other = (CompositeModification) obj;
		if (!Arrays.equals(modifications, other.modifications))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (undoOrder != other.undoOrder)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompositeModification [title=" + title + ", undoOrder=" + undoOrder + ", modifications="
				+ Arrays.toString(modifications) + "]";
	}

}
