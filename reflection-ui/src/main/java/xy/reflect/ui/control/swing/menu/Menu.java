/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.AbstractMenuItemInfo;
import xy.reflect.ui.info.menu.CustomActionMenuItemInfo;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MethodActionMenuItemInfo;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo.Type;
import xy.reflect.ui.util.KeyboardKey;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Menu item container class.
 * 
 * @author olitank
 *
 */
public class Menu extends JMenu {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Form menuBarOwner;
	protected MenuInfo menuInfo;

	public Menu(SwingRenderer swingRenderer, Form menuBarOwner, MenuInfo menuInfo) {
		super(menuInfo.getCaption());
		this.swingRenderer = swingRenderer;
		this.menuBarOwner = menuBarOwner;
		this.menuInfo = menuInfo;
		initialize();
	}

	public void refresh() {
		removeAll();
		configure();
	}

	protected void initialize() {
		customizeUI();
		configure();
	}

	protected void configure() {
		for (int i = 0; i < menuInfo.getItemCategories().size(); i++) {
			if (i > 0) {
				add(createSeparator());
			}
			MenuItemCategory category = menuInfo.getItemCategories().get(i);
			for (AbstractMenuItemInfo item : category.getItems()) {
				add(createMenuItem(item));
			}
		}
		if (menuInfo.getItems().size() > 0) {
			if (getSubElements().length > 0) {
				add(createSeparator());
			}
			for (AbstractMenuItemInfo item : menuInfo.getItems()) {
				add(createMenuItem(item));
			}
		}
		int mnemonic = computeMnemonic();
		if (mnemonic != -1) {
			setMnemonic(mnemonic);
		}
	}

	protected void customizeUI() {
		Color backgroundColor = menuBarOwner.getControlsBackgroundColor();
		Color foregroundColor = menuBarOwner.getControlsForegroundColor();
		Font labelCustomFont = menuBarOwner.getLabelCustomFont();
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
		if (isTopLevelMenu()) {
			/*
			 * Windows top-level menus may be transparent (no background color) so that an
			 * eventual background image would be visible through them. Note that the menu
			 * bar may also be transparent.
			 */
			setOpaque(backgroundColor != null);
		} else {
			setOpaque(true);
		}
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		}
		if (labelCustomFont != null) {
			setFont(labelCustomFont.deriveFont(getFont().getStyle(), getFont().getSize()));
		}
	}

	protected int computeMnemonic() {
		KeyboardKey mnemonicKey = menuInfo.getMnemonicKey();
		if (mnemonicKey == null) {
			return -1;
		}
		return mnemonicKey.getKeyCode();
	}

	protected JSeparator createSeparator() {
		return new MenuItemSeparator(swingRenderer, menuBarOwner);
	}

	protected JMenuItem createMenuItem(AbstractMenuItemInfo itemInfo) {
		if (itemInfo instanceof StandardActionMenuItemInfo) {
			return createActionMenuItem((StandardActionMenuItemInfo) itemInfo);
		} else if (itemInfo instanceof MethodActionMenuItemInfo) {
			return createActionMenuItem((MethodActionMenuItemInfo) itemInfo);
		} else if (itemInfo instanceof CustomActionMenuItemInfo) {
			return createActionMenuItem((CustomActionMenuItemInfo) itemInfo);
		} else if (itemInfo instanceof MenuInfo) {
			return new Menu(swingRenderer, menuBarOwner, (MenuInfo) itemInfo);
		} else {
			throw new ReflectionUIError("Unhandled menu item type: '" + itemInfo + "'");
		}
	}

	protected JMenuItem createActionMenuItem(final CustomActionMenuItemInfo actionMenuItemInfo) {
		return new CustomActionMenuItem(swingRenderer, menuBarOwner, actionMenuItemInfo);
	}

	protected JMenuItem createActionMenuItem(final MethodActionMenuItemInfo actionMenuItemInfo) {
		return new MethodActionMenuItem(swingRenderer, menuBarOwner, actionMenuItemInfo);
	}

	protected JMenuItem createActionMenuItem(final StandardActionMenuItemInfo menuItemInfo) {
		if (menuItemInfo.getType() == Type.NEW) {
			return new RenewMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.OPEN) {
			return new OpenMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.SAVE) {
			return new SaveMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.SAVE_AS) {
			return new SaveAsMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.UNDO) {
			return new UndoMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.REDO) {
			return new RedoMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.RESET) {
			return new ResetMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.HELP) {
			return new HelpMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else if (menuItemInfo.getType() == Type.EXIT) {
			return new CloseWindowMenuItem(swingRenderer, menuBarOwner, menuItemInfo);
		} else {
			throw new ReflectionUIError();
		}
	}

}
