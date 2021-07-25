/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.AbstractMenuItemInfo;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MethodActionMenuItemInfo;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo.Type;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Menu item container class.
 * 
 * @author olitank
 *
 */
public class Menu extends JMenu {

	private static final long serialVersionUID = 1L;

	private SwingRenderer swingRenderer;
	private MenuInfo menuInfo;

	public Menu(SwingRenderer swingRenderer, MenuInfo menuInfo) {
		super(menuInfo.getCaption());
		this.swingRenderer = swingRenderer;
		this.menuInfo = menuInfo;
		initialize();
	}

	protected void initialize() {
		for (int i = 0; i < menuInfo.getItemCategories().size(); i++) {
			if (i > 0) {
				addSeparator();
			}
			MenuItemCategory category = menuInfo.getItemCategories().get(i);
			for (AbstractMenuItemInfo item : category.getItems()) {
				add(createMenuItem(item));
			}
		}
		if (menuInfo.getItems().size() > 0) {
			if (getSubElements().length > 0) {
				addSeparator();
			}
			for (AbstractMenuItemInfo item : menuInfo.getItems()) {
				add(createMenuItem(item));
			}
		}
	}

	protected JMenuItem createMenuItem(AbstractMenuItemInfo itemInfo) {
		if (itemInfo instanceof StandradActionMenuItemInfo) {
			return createActionMenuItem((StandradActionMenuItemInfo) itemInfo);
		} else if (itemInfo instanceof MethodActionMenuItemInfo) {
			return createActionMenuItem((MethodActionMenuItemInfo) itemInfo);
		} else if (itemInfo instanceof MenuInfo) {
			return new Menu(swingRenderer, (MenuInfo) itemInfo);
		} else {
			throw new ReflectionUIError("Unhandled menu item type: '" + itemInfo + "'");
		}
	}

	protected JMenuItem createActionMenuItem(final MethodActionMenuItemInfo actionMenuItemInfo) {
		Form contextForm = (Form) actionMenuItemInfo.getSpecificProperties().get(Form.ACTION_MENU_ITEM_CONTEXT_FORM);
		return new MethodActionMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
	}

	protected JMenuItem createActionMenuItem(final StandradActionMenuItemInfo actionMenuItemInfo) {
		Form contextForm = (Form) actionMenuItemInfo.getSpecificProperties().get(Form.ACTION_MENU_ITEM_CONTEXT_FORM);
		if (actionMenuItemInfo.getType() == Type.OPEN) {
			return new OpenMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.SAVE) {
			return new SaveMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.SAVE_AS) {
			return new SaveAsMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.UNDO) {
			return new UndoMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.REDO) {
			return new RedoMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.RESET) {
			return new ResetMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.HELP) {
			return new HelpMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else if (actionMenuItemInfo.getType() == Type.EXIT) {
			return new CloseWindowMenuItem(swingRenderer, contextForm, actionMenuItemInfo);
		} else {
			throw new ReflectionUIError();
		}
	}

}
