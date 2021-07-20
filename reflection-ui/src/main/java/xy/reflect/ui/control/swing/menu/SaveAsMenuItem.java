


package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;

/**
 * Menu item that allows to save an object state to a different file.
 * 
 * @author olitank
 *
 */
public class SaveAsMenuItem extends AbstractSaveMenuItem {

	protected static final long serialVersionUID = 1L;

	public SaveAsMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

}
