package xy.reflect.ui.info.menu.builtin;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class ExitMenuItem extends AbstractBuiltInActionMenuItem {

	public ExitMenuItem() {
		name = "Exit";
	}

	@Override
	public void execute(Object form, Object renderer) {
		Window window = SwingUtilities.getWindowAncestor((JPanel)form);
		WindowEvent closeEvent = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

}
