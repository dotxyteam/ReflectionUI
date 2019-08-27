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
package xy.reflect.ui.control;

import javax.swing.SwingUtilities;

import xy.reflect.ui.util.ReflectionUIError;

public abstract class ErrorHandlingFieldControlData extends FieldControlDataProxy {

	protected Object lastFieldValue;
	protected boolean lastFieldValueInitialized = false;
	protected Throwable lastValueUpdateError;

	protected abstract void displayError(Throwable t);

	public ErrorHandlingFieldControlData(IFieldControlData base) {
		super(base);
	}

	@Override
	public Object getValue() {
		try {
			if (lastValueUpdateError != null) {
				throw lastValueUpdateError;
			}
			lastFieldValue = super.getValue();
			lastFieldValueInitialized = true;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					displayError(null);
				}
			});
		} catch (final Throwable t) {
			if (!lastFieldValueInitialized) {
				throw new ReflectionUIError(t);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						displayError(t);
					}
				});
			}
		}
		return lastFieldValue;

	}

	@Override
	public void setValue(Object newValue) {
		try {
			lastFieldValue = newValue;
			super.setValue(newValue);
			lastValueUpdateError = null;
		} catch (Throwable t) {
			lastValueUpdateError = t;
		}
	}
}
