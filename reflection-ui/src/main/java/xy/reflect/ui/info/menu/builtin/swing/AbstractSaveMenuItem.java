package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public abstract class  AbstractSaveMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

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

	@Override
	protected File retrieveFile(SwingRenderer swingRenderer, JPanel form) {
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
