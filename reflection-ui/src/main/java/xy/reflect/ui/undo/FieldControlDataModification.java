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
package xy.reflect.ui.undo;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldControlDataModification extends AbstractModification {

	protected IFieldControlData data;
	protected Object newValue;

	public FieldControlDataModification(final IFieldControlData data, final Object newValue) {
		check(data);
		this.data = data;
		this.newValue = newValue;
	}

	protected void check(IFieldControlData data) {
		if (Boolean.TRUE.equals(data.getSpecificProperties()
				.get(FieldControlPlaceHolder.COMMON_CONTROL_MANAGEMENT_ENABLED_PROPERTY_KEY))) {
			throw new ReflectionUIError("A " + FieldControlDataModification.class.getSimpleName()
					+ " must not be constructed with a data that has the common control management enabled");
		}
	}

	public static String getTitle(String targetCaption) {
		if ((targetCaption == null) || (targetCaption.length() == 0)) {
			return "";
		}
		return "Edit '" + targetCaption + "'";
	}

	@Override
	public String getTitle() {
		return getTitle(data.getCaption());
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(newValue);
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		return ReflectionUIUtils.getUndoJob(data, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		FieldControlDataModification other = (FieldControlDataModification) obj;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlDataModification [data=" + data + ", newValue=" + newValue + "]";
	}

}
