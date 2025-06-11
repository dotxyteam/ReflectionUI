package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import javax.swing.JSeparator;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class MenuItemSeparator extends JSeparator {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Form menuBarOwner;

	public MenuItemSeparator(SwingRenderer swingRenderer, Form menuBarOwner) {
		this.swingRenderer = swingRenderer;
		this.menuBarOwner = menuBarOwner;
		initialize();
	}

	protected void initialize() {
		customizeUI();
	}

	protected void customizeUI() {
		Color backgroundColor = menuBarOwner.getControlsBackgroundColor();
		Color foregroundColor = menuBarOwner.getControlsForegroundColor();
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
		setOpaque(true);
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		}
	}
}
