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
package xy.reflect.ui.util;

/**
 * Main exception class of the library.
 * 
 * @author olitank
 *
 */
public class ReflectionUIError extends RuntimeException {
	protected static final long serialVersionUID = 1L;

	public ReflectionUIError() {
		super("ReflectionUI Internal Error. Check the logs for more information");
	}

	public ReflectionUIError(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionUIError(String message) {
		super(message);
	}

	public ReflectionUIError(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		String result = super.getMessage();
		if (result == null) {
			result = MiscUtils.getPrintedStackTrace(this);
		}
		if (getCause() != null) {
			String causeClassName = getCause().getClass().getName();
			if (result.contains(causeClassName)) {
				String causeClassCaption;
				if (NullPointerException.class.getName().equals(causeClassName)) {
					causeClassCaption = "Missing Value Error";
				} else {
					causeClassCaption = ReflectionUIUtils.identifierToCaption(getCause().getClass().getSimpleName());
				}
				if (!causeClassCaption.contains("Error") && causeClassCaption.contains("Exception")) {
					causeClassCaption = causeClassCaption.replace("Exception", "Error");
				}
				result = result.replace(causeClassName, causeClassCaption);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
