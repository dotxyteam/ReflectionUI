


package xy.reflect.ui.control.swing.menu;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;

/**
 * Menu item that allows to close the current window.
 * 
 * @author olitank
 *
 */
public class CloseWindowMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	public CloseWindowMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected void execute() {
		Window window = SwingUtilities.getWindowAncestor(form);
		WindowEvent closeEvent = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

	@Override
	protected boolean isActive() {
		return true;
	}

}
