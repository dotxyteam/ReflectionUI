package xy.reflect.ui.info.menu.builtin.swing;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;


public class CloseWindowMenuItem extends AbstractBuiltInActionMenuItem {

	public CloseWindowMenuItem() {
		name = "Exit";
	}

	@Override
	public void execute(Object form, Object renderer) {
		Window window = SwingUtilities.getWindowAncestor((JPanel)form);
		WindowEvent closeEvent = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

}
