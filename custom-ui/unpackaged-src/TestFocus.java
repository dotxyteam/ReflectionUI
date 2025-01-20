import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TestFocus {

	public static void main(String[] args) {
		JPanel panel = new JPanel();
		panel.setFocusable(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (int i = 0; i < 5; i++) {
			JTextField component = new JTextField();
			panel.add(component);
		}
		JDialog dialog = new JDialog();
		dialog.getContentPane().add(panel);
		dialog.setSize(new Dimension(300, 300));
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				new Thread() {

					@Override
					public void run() {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								requestAnyComponentFocus(panel);
							}
						});
					}

				}.start();
			}
		});
		dialog.setVisible(true);
	}

	public static boolean requestAnyComponentFocus(Component c) {
		if (!c.isEnabled()) {
			return false;
		}
		if (c.hasFocus()) {
			System.out.println("hasFocus: " + c);
			return true;
		}
		if (c.requestFocusInWindow()) {
			System.out.println("requestFocusInWindow: " + c);
			return true;
		}
		if (c instanceof Container) {
			try {
				for (Component child : ((Container) c).getComponents()) {
					if (requestAnyComponentFocus(child)) {
						return true;
					}
				}
			} catch (Throwable ignore) {
				ignore.printStackTrace();
			}
		}
		return false;
	}

}
