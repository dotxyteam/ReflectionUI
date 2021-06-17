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
package xy.reflect.ui.info.method;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Virtual method allowing to execute the
 * {@link ITypeInfo#save(Object, OutputStream)} method.
 * 
 * @author olitank
 *
 */
public class SaveToFileMethod extends AbstractPersistenceMethod {

	public SaveToFileMethod(ReflectionUI reflectionUI, ITypeInfo containingType) {
		super(reflectionUI, containingType);
	}

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public String getCaption() {
		return "Save";
	}

	@Override
	public String getOnlineHelp() {
		return "Saves to a file";
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		File file = (File) invocationData.getParameterValue(0);
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			containingType.save(object, out);
		} catch (FileNotFoundException e) {
			throw new ReflectionUIError(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String toString() {
		return "SaveToFileMethod [containingType=" + containingType + "]";
	}

}
