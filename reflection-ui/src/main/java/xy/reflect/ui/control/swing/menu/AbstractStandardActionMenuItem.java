/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;

/**
 * Base class for standard menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractStandardActionMenuItem extends AbstractMenuItem {

	private static final long serialVersionUID = 1L;

	protected StandradActionMenuItemInfo menuItemInfo;

	protected abstract void execute();

	protected abstract boolean isActive();

	public AbstractStandardActionMenuItem(SwingRenderer swingRenderer, Form menuBarOwner,
			StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner);
		this.menuItemInfo = menuItemInfo;
		initialize();
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Form getContextForm() {
		if (menuItemInfo == null) {
			return null;
		}
		return (Form) menuItemInfo.getSpecificProperties().get(Form.ACTION_MENU_ITEM_CONTEXT_FORM);
	}

	public StandradActionMenuItemInfo getMenuItemInfo() {
		return menuItemInfo;
	}

	protected void initialize() {
		setAction(createAction());
		try {
			setText(menuItemInfo.getCaption());
			if (!isActive()) {
				setEnabled(false);
			}
			Image image = swingRenderer.getMenuItemIconImage(menuItemInfo);
			ImageIcon icon;
			if (image != null) {
				icon = SwingRendererUtils.getSmallIcon(SwingRendererUtils.getIcon(image));
			} else {
				icon = null;
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

	protected Action createAction() {
		return new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					execute();
				} catch (Throwable t) {
					swingRenderer.handleException(menuBarOwner, t);
				}
			}
		};
	}

}
