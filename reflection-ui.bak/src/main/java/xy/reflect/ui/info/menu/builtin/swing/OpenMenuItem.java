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
package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

public class OpenMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public OpenMenuItem() {
		name = "Open...";
		fileBrowserConfiguration.actionTitle = "Open";
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			type.load(object, in);
		} catch (Throwable t) {
			throw new ReflectionUIError(t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable ignore) {
				}
			}
			ModificationStack modifStack = form.getModificationStack();
			modifStack.forget();
		}
	}

}
