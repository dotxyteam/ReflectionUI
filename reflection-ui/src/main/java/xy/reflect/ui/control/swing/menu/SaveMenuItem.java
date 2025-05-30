
package xy.reflect.ui.control.swing.menu;

import java.io.File;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;

/**
 * Menu item that allows to save an object state to a file.
 * 
 * @author olitank
 *
 */
public class SaveMenuItem extends AbstractSaveMenuItem {

	protected static final long serialVersionUID = 1L;

	public SaveMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected boolean isActive() {
		if (isFileSynchronized()) {
			return false;
		}
		return super.isActive();
	}

	@Override
	protected File retrieveFile() {
		File file = lastFileByForm.get(getContextForm());
		if (file != null) {
			return file;
		}
		return super.retrieveFile();
	}

	@Override
	public String getText() {
		String result = super.getText();
		File file = lastFileByForm.get(getContextForm());
		if (file != null) {
			result += " " + file.getPath();
		}
		return result;
	}

}
