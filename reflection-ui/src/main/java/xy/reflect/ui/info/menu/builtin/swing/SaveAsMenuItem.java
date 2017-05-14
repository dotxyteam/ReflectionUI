package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class SaveAsMenuItem extends AbstractPersistenceMenuItem {

	protected static final long serialVersionUID = 1L;

	public SaveAsMenuItem() {
		name = "Save As...";
		fileBrowserConfiguration.actionTitle = "Save";
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final JPanel form, File file) {
		Object object = swingRenderer.getObjectByForm().get(form);
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

}
