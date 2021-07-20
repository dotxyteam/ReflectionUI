


package xy.reflect.ui.control.swing.menu;

import java.awt.Image;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
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

	public HelpMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected void execute() {
		Object object = form.getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.buildTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			throw new ReflectionUIError("Online help not provided for the type '" + type.getName() + "'");
		}
		String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object),
				menuItemInfo.getCaption());
		Image iconImage = swingRenderer.getObjectIconImage(object);
		swingRenderer.openInformationDialog((JPanel) form, onlineHelp, title, iconImage);
	}

	@Override
	protected boolean isActive() {
		return true;
	}

}
