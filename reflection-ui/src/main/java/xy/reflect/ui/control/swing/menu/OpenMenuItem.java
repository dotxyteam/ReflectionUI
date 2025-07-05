
package xy.reflect.ui.control.swing.menu;

import java.io.File;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Menu item that allows to load an object state from a file.
 * 
 * @author olitank
 *
 */
public class OpenMenuItem extends AbstractFileMenuItem {

	protected static final long serialVersionUID = 1L;

	public OpenMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
	}

	@Override
	public void execute() {
		if (!isFileSynchronized()) {
			if (!swingRenderer.openQuestionDialog(menuBarOwner,
					"Changes were not saved and will be lost.\nContinue?",
					fileBrowserConfiguration.actionTitle, "OK", "Cancel")) {
				return;
			}
		}
		super.execute();
	}

	@Override
	protected void persist(final SwingRenderer swingRenderer, final Form form, File file) {
		Object object = form.getObject();
		final ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));		
		swingRenderer.showBusyDialogWhile(form, new Runnable() {
			@Override
			public void run() {
				type.load(object, file);
			}
		}, ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Loading..."));
		form.getModificationStack().forget();
	}

}
