
package xy.reflect.ui.control.swing.menu;

import java.io.File;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Base class for "save" and "save as" menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSaveMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public AbstractSaveMenuItem(SwingRenderer swingRenderer, Form menuBarOwner,
			StandardActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		final ITypeInfo type = menuItemInfo.getObjectType();
		swingRenderer.showBusyDialogWhile(form, new Runnable() {
			@Override
			public void run() {
				type.save(object, file);
			}
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Saving..."));
	}

	@Override
	protected File retrieveFile() {
		File result = super.retrieveFile();
		if (result != null) {
			if (result.exists()) {
				if (!swingRenderer.openQuestionDialog(menuBarOwner,
						"The file '" + result.getPath() + "' already exists.\nDo you want to replace it?",
						fileBrowserConfiguration.actionTitle, "OK", "Cancel")) {
					result = null;
				}
			}
		}
		return result;
	}

}
