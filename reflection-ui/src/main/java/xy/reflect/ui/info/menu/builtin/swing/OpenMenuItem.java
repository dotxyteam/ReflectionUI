package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

public class OpenMenuItem extends AbstractPersistenceMenuItem {

	protected static final long serialVersionUID = 1L;

	public OpenMenuItem() {
		name = "Open...";
		fileBrowserConfiguration.actionTitle = "Open";
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final JPanel form, File file) {
		Object object = swingRenderer.getObjectByForm().get(form);
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
			ModificationStack modifStack = swingRenderer.getModificationStackByForm().get(form);
			modifStack.invalidate();		
		}
	}

	
}
