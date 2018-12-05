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
import java.io.FileOutputStream;
import java.io.OutputStream;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public abstract class  AbstractSaveMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			type.save(object, out);
		} catch (Throwable t) {
			throw new ReflectionUIError(t);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Throwable ignore) {
				}
			}
		}
	}

	@Override
	protected File retrieveFile(SwingRenderer swingRenderer, Form form) {
		File result = super.retrieveFile(swingRenderer, form);
		if (result != null) {
			if (result.exists()) {
				if (!swingRenderer.openQuestionDialog(form,
						"The file '" + result.getPath() + "' already exists.\nDo you want to replace it?",
						fileBrowserConfiguration.actionTitle, "OK", "Cancel")) {
					result = null;
				}
			}
		}
		return result;
	}

}
