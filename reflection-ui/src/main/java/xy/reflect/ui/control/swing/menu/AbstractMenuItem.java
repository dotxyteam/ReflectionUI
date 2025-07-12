/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

/**
 * Base class for all menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	public abstract void refresh();

	protected SwingRenderer swingRenderer;
	protected Form menuBarOwner;

	public AbstractMenuItem(SwingRenderer swingRenderer, Form menuBarOwner) {
		this.swingRenderer = swingRenderer;
		this.menuBarOwner = menuBarOwner;
		customizeUI();
	}

	protected void customizeUI() {
		Color backgroundColor = menuBarOwner.getControlsBackgroundColor();
		Color foregroundColor = menuBarOwner.getControlsForegroundColor();
		Font labelCustomFont = menuBarOwner.getLabelCustomFont();
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
		setOpaque(true);
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		}
		if (labelCustomFont != null) {
			setFont(labelCustomFont.deriveFont(getFont().getStyle(), getFont().getSize()));
		}
	}

}
