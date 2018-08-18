package xy.reflect.ui.util.component;

import java.awt.Component;

import javax.swing.JScrollPane;

public class ControlScrollPane extends JScrollPane {

	private static final long serialVersionUID = 1L;

	public ControlScrollPane() {
		setOpaque(false);
		getViewport().setOpaque(false);
	}

	public ControlScrollPane(Component view) {
		super(view);
		setOpaque(false);
		getViewport().setOpaque(false);
	}

}
