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
package xy.reflect.ui.control.swing.util;

import java.awt.Component;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control data that handle value access errors by notifying them and
 * returning the last valid value.
 * 
 * @author olitank
 *
 */
public class ErrorHandlingFieldControlData extends FieldControlDataProxy {

	protected SwingRenderer swingRenderer;
	protected Component errorDialogOwner;

	protected Object lastFieldValue;
	protected boolean lastFieldValueInitialized = false;
	protected Throwable lastValueUpdateError;
	protected String currentlyDisplayedErrorId;

	public ErrorHandlingFieldControlData(IFieldControlData data, SwingRenderer swingRenderer,
			Component errorDialogOwner) {
		super(data);
		this.swingRenderer = swingRenderer;
		this.errorDialogOwner = errorDialogOwner;
	}

	@Override
	public Object getValue() {
		try {
			if (lastValueUpdateError != null) {
				throw lastValueUpdateError;
			}
			lastFieldValue = super.getValue();
			lastFieldValueInitialized = true;
			handleError(null);
		} catch (final Throwable t) {
			if (!lastFieldValueInitialized) {
				throw new ReflectionUIError(t);
			} else {
				handleError(t);
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

	/**
	 * Called to notify an error.
	 * 
	 * @param t The exception that was thrown or null if the error is gone.
	 */
	protected void handleError(Throwable t) {
		final String newErrorId = (t == null) ? null : MiscUtils.getPrintedStackTrace(t);
		if (MiscUtils.equalsOrBothNull(newErrorId, currentlyDisplayedErrorId)) {
			return;
		}
		currentlyDisplayedErrorId = newErrorId;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (t != null) {
					currentlyDisplayedErrorId = newErrorId;
					swingRenderer.handleExceptionsFromDisplayedUI(errorDialogOwner, t);
				} else {
					currentlyDisplayedErrorId = null;
				}
			}
		});
	}

}
