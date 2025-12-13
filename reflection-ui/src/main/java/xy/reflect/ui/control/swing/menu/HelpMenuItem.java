
package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Menu item that allows to display the current object type online help.
 * 
 * @author olitank
 *
 */
public class HelpMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	public HelpMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandardActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
	}

	@Override
	protected void execute() {
		Object object = getContextForm().getObject();
		ITypeInfo type = menuItemInfo.getObjectType();
		String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			throw new ReflectionUIError("Online help not provided for the type '" + type.getName() + "'");
		}
		String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object),
				menuItemInfo.getCaption());
		swingRenderer.openInformationDialog(menuBarOwner, onlineHelp, title);
	}

	@Override
	protected boolean isActive() {
		return true;
	}

}
