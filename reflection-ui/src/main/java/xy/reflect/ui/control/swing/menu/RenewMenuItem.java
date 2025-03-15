
package xy.reflect.ui.control.swing.menu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Menu item that allows to renew an object state.
 * 
 * @author olitank
 *
 */
public class RenewMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public RenewMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected void check() {
	}

	@Override
	protected File retrieveFile() {
		throw new ReflectionUIError();
	}

	@Override
	public void execute() {
		if (!isFileSynchronized()) {
			if (!swingRenderer.openQuestionDialog(form, "Changes were not saved and will be lost.\nContinue?",
					ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(form.getObject()),
							getMenuItemInfo().getCaption()),
					"OK", "Cancel")) {
				return;
			}
		}
		processFile(null);
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		final ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		swingRenderer.showBusyDialogWhile(form, new Runnable() {
			@Override
			public void run() {
				InputStream in = null;
				try {
					Object newObject = ReflectionUIUtils.createDefaultInstance(type);
					ByteArrayOutputStream newObjectStore = new ByteArrayOutputStream();
					type.save(newObject, newObjectStore);
					in = new ByteArrayInputStream(newObjectStore.toByteArray());
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
				}
			}
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Renewing..."));
		form.getModificationStack().forget();
	}

}
