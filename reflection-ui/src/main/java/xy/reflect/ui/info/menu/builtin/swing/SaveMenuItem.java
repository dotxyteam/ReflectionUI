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
package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;

import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.undo.ModificationStack;

public class SaveMenuItem extends AbstractSaveMenuItem {

	protected static final long serialVersionUID = 1L;

	protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

	public SaveMenuItem() {
		name = "Save";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		if (isFileSynchronized((Form) form)) {
			return false;
		}
		return super.isEnabled(form, renderer);
	}

	public boolean isFileSynchronized(Form form) {
		ModificationStack modifStack = form.getModificationStack();
		Long lastSavedVersion = lastPersistedVersionByForm.get(form);
		if (lastSavedVersion == null) {
			if (modifStack.getStateVersion() == 0) {
				return true;
			}
		} else {
			if (lastSavedVersion.equals(modifStack.getStateVersion())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName(final Object form, final Object renderer) {
		String result = super.getName(form, renderer);
		File file = lastFileByForm.get((Form) form);
		if (file != null) {
			result += " " + file.getPath();
		}
		return result;
	}

	@Override
	protected File retrieveFile(final SwingRenderer swingRenderer, Form form) {
		File file = lastFileByForm.get(form);
		if (file != null) {
			return file;
		}
		return super.retrieveFile(swingRenderer, form);
	}
}
