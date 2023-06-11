/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Base class for standard menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractStandardActionMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Form form;
	protected StandradActionMenuItemInfo menuItemInfo;

	protected abstract void execute();

	protected abstract boolean isActive();

	public AbstractStandardActionMenuItem(SwingRenderer swingRenderer, Form form,
			StandradActionMenuItemInfo menuItemInfo) {
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.menuItemInfo = menuItemInfo;
		initialize();
	}

	protected void initialize() {
		customizeUI();		
		setAction(createAction());
		try {
			setText(menuItemInfo.getCaption());
			if (!isActive()) {
				setEnabled(false);
			}
			ImageIcon icon = swingRenderer.getMenuItemIcon(menuItemInfo);
			if (icon != null) {
				icon = SwingRendererUtils.getSmallIcon(icon);
			}
			setIcon(icon);
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			if (getText() == null) {
				setText(t.toString());
			} else {
				setText(getText() + "(" + t.toString() + ")");
			}
			setEnabled(false);
		}
	}

	protected void customizeUI() {
		Color awtBackgroundColor = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getMainBackgroundColor() != null)
						? SwingRendererUtils
								.getColor(swingRenderer.getReflectionUI().getApplicationInfo().getMainBackgroundColor())
						: null;
		Color awtForegroundColor = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getMainForegroundColor() != null)
						? SwingRendererUtils
								.getColor(swingRenderer.getReflectionUI().getApplicationInfo().getMainForegroundColor())
						: null;
		Font labelCustomFont = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getLabelCustomFontResourcePath() != null)
						? SwingRendererUtils
								.loadFontThroughCache(
										swingRenderer.getReflectionUI().getApplicationInfo()
												.getLabelCustomFontResourcePath(),
										ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
								.deriveFont(getFont().getStyle(), getFont().getSize())
						: null;
		if (awtBackgroundColor != null) {
			setBackground(awtBackgroundColor);
		}
		if (awtForegroundColor != null) {
			setForeground(awtForegroundColor);
		}
		if (labelCustomFont != null) {
			setFont(labelCustomFont);
		}

	}

	protected Action createAction() {
		return new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					execute();
				} catch (Throwable t) {
					swingRenderer.handleObjectException(form, t);
				}
			}
		};
	}

}
