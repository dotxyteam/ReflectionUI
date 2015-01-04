package xy.reflect.ui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.UIManager;

import xy.reflect.ui.ReflectionUI;

public class NullControl extends JTextField {

	protected static final long serialVersionUID = 1L;

	public NullControl(final ReflectionUI reflectionUI, final Runnable onMousePress) {
		if (onMousePress != null) {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					try {
						onMousePress.run();
					} catch (Throwable t) {
						reflectionUI.handleExceptionsFromDisplayedUI(
								NullControl.this, t);
					}
				}
			});
		}
		setEditable(false);
		setBackground(UIManager.getColor("TextField.shadow"));		
		setBorder(null);
	}

}
