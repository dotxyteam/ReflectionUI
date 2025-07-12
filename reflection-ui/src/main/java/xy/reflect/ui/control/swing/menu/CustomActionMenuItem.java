/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.CustomActionMenuItemInfo;
import xy.reflect.ui.util.KeyboardShortcut;

/**
 * Menu item that allows to invoke a method.
 * 
 * @author olitank
 *
 */
public class CustomActionMenuItem extends AbstractMenuItem {

	private static final long serialVersionUID = 1L;

	protected CustomActionMenuItemInfo menuItemInfo;

	public CustomActionMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, CustomActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner);
		this.menuItemInfo = menuItemInfo;
		configure();
	}

	@Override
	public void refresh() {
		configure();
	}

	protected void configure() {
		setAction(createAction());
		setAccelerator(createAccelerator());
		try {
			setText(menuItemInfo.getCaption());
			Image image = swingRenderer.getMenuItemIconImage(menuItemInfo);
			ImageIcon icon;
			if (image != null) {
				icon = SwingRendererUtils.getSmallIcon(SwingRendererUtils.getIcon(image));
			} else {
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

	protected KeyStroke createAccelerator() {
		KeyboardShortcut keyboardShortcut = menuItemInfo.getKeyboardShortcut();
		if (keyboardShortcut == null) {
			return null;
		}
		return KeyStroke.getKeyStroke(keyboardShortcut.getKeyCode(), keyboardShortcut.getModifiers());
	}

	protected Action createAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					menuItemInfo.getRunnable().run();
				} catch (Throwable t) {
					swingRenderer.handleException(menuBarOwner, t);
				}
			}

		};
	}

}
