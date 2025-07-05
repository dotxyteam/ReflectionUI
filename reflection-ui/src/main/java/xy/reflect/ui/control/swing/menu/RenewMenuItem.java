
package xy.reflect.ui.control.swing.menu;

import java.io.File;
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

	public RenewMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandradActionMenuItemInfo menuItemInfo) {
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
		final ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		Object newObject = swingRenderer.onTypeInstantiationRequest(form, type);
		if(newObject == null) {
			return;
		}
		form.setObject(newObject);
		form.getModificationStack().forget();
	}

}
