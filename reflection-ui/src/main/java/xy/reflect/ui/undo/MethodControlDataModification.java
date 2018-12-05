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

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.util.ReflectionUIError;

public class MethodControlDataModification extends AbstractModification {

	protected IMethodControlData data;
	protected InvocationData invocationData;

	public MethodControlDataModification(IMethodControlData data, InvocationData invocationData) {
		this.data = data;
		this.invocationData = invocationData;
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				data.invoke(invocationData);
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		Runnable result = data.getNextUpdateCustomUndoJob(invocationData);
		if (result == null) {
			throw new ReflectionUIError();
		}
		return result;
	}

	@Override
	public String getTitle() {
		return getTitle(data.getCaption());
	}

	public static String getTitle(String targetCaption) {
		return "Execute '" + targetCaption + "'";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
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
		MethodControlDataModification other = (MethodControlDataModification) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (invocationData == null) {
			if (other.invocationData != null)
				return false;
		} else if (!invocationData.equals(other.invocationData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodControlDataModification [data=" + data + ", invocationData=" + invocationData + "]";
	}

}
