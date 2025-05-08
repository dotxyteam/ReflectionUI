/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.CustomActionMenuItemInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Menu item that allows to invoke a method.
 * 
 * @author olitank
 *
 */
public class CustomActionMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Form form;
	protected CustomActionMenuItemInfo menuItemInfo;

	public CustomActionMenuItem(SwingRenderer swingRenderer, Form form, CustomActionMenuItemInfo menuItemInfo) {
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
			Image image = swingRenderer.getMenuItemIconImage(menuItemInfo);
			ImageIcon icon;
			if (image != null) {
				icon = SwingRendererUtils.getSmallIcon(SwingRendererUtils.getIcon(image));
			}else {
				icon = null;
			}
			setIcon(icon);
			setEnabled(menuItemInfo.getEnablementStateSupplier().get());
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
					menuItemInfo.getRunnable().run();
				} catch (Throwable t) {
					swingRenderer.handleException(form, t);
				}
			}

		};
	}

}
