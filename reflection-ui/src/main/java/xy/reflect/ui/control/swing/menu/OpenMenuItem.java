


package xy.reflect.ui.control.swing.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Menu item that allows to load an object state from a file.
 * 
 * @author olitank
 *
 */
public class OpenMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public OpenMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
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
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Loading..."));
	}

}
