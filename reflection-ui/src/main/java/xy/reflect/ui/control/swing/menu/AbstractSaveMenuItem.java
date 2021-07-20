


package xy.reflect.ui.control.swing.menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Base class for "save" and "save as" menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSaveMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public AbstractSaveMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		final ITypeInfo type = swingRenderer.getReflectionUI()
				.buildTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		swingRenderer.showBusyDialogWhile(form, new Runnable() {
			@Override
			public void run() {
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
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Saving..."));

	}

	@Override
	protected File retrieveFile() {
		File result = super.retrieveFile();
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
