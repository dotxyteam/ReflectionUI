
package xy.reflect.ui.control.swing.menu;

import java.io.File;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.IOUtils;
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

	public RenewMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandardActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
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
			if (!swingRenderer.openQuestionDialog(menuBarOwner, "Changes were not saved and will be lost.\nContinue?",
					ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(getContextForm().getObject()),
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
		final ITypeInfo type = menuItemInfo.getObjectType();
		Object newObject = swingRenderer.onTypeInstantiationRequest(form, type);
		if (newObject == null) {
			return;
		}
		swingRenderer.showBusyDialogWhile(form, new Runnable() {
			@Override
			public void run() {
				try {
					File tmpFile = IOUtils.createTemporaryFile();
					try {
						type.save(newObject, tmpFile);
						type.load(object, tmpFile);
					} finally {
						IOUtils.delete(tmpFile);
					}
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			}
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Renewing..."));
		form.getModificationStack().forget();
	}

}
