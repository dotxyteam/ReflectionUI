package xy.reflect.ui.menu;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MenuModel {

	protected List<Menu> menus = new ArrayList<Menu>();

	public List<Menu> getMenus() {
		return menus;
	}

	public void add(IMenuElementPosition position, Runnable action) {
		ActionMenuItem menuItem = createActionMenuItem(position.getElementName(), action);
		if (position.getParent() == null) {
			throw new ReflectionUIError("No container found for menu element: " + position);
		} else {
			IMenuItemContainer container = addMenuElementContainer(position.getParent());
			addInContainer(menuItem, container);
		}
	}

	protected void addInContainer(IMenuElement menuElement, IMenuItemContainer container) {
		if (container instanceof Menu) {
			if (menuElement instanceof AbstractMenuItem) {
				((Menu) container).addItem((AbstractMenuItem) menuElement);
			} else if (menuElement instanceof MenuItemCategory) {
				((Menu) container).addItemCategory((MenuItemCategory) menuElement);
			} else {
				throw new ReflectionUIError();
			}
		} else if (container instanceof MenuItemCategory) {
			if (menuElement instanceof AbstractMenuItem) {
				((MenuItemCategory) container).addItem((AbstractMenuItem) menuElement);
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
	}

	protected ActionMenuItem createActionMenuItem(String name, Runnable action) {
		return new ActionMenuItem(name, action);
	}

	protected IMenuItemContainer addMenuElementContainer(IMenuElementPosition containerPosition) {
		IMenuItemContainer container = createContainer(containerPosition);
		if (containerPosition.getParent() == null) {
			if (containerPosition.getElementKind() != MenuElementKind.MENU) {
				throw new ReflectionUIError("Unexpected menu element at root position: " + containerPosition);
			}
			for (Menu menu : menus) {
				if (same(container, menu)) {
					return menu;
				}
			}
			menus.add((Menu) container);
		} else {
			IMenuItemContainer containerParent = addMenuElementContainer(containerPosition.getParent());
			for (AbstractMenuItem item : containerParent.getItems()) {
				if (same(container, item)) {
					return (IMenuItemContainer) item;
				}
			}
			if (containerParent instanceof Menu) {
				for (MenuItemCategory category : ((Menu) containerParent).getItemCategories()) {
					if (same(container, category)) {
						return category;
					}
				}
			}
			addInContainer(container, containerParent);
		}
		return container;
	}

	protected boolean same(IMenuElement menuElement1, IMenuElement menuElement2) {
		if (ReflectionUIUtils.getMenuElementKind(menuElement1) == ReflectionUIUtils.getMenuElementKind(menuElement2)) {
			if (menuElement1.getName().equals(menuElement2.getName())) {
				return true;
			}
		}
		return false;
	}

	protected IMenuItemContainer createContainer(IMenuElementPosition containerPosition) {
		if (containerPosition.getElementKind() == MenuElementKind.MENU) {
			return new Menu(containerPosition.getElementName());
		} else if (containerPosition.getElementKind() == MenuElementKind.ITEM_CATEGORY) {
			return new MenuItemCategory(containerPosition.getElementName());
		} else {
			throw new ReflectionUIError();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((menus == null) ? 0 : menus.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuModel other = (MenuModel) obj;
		if (menus == null) {
			if (other.menus != null)
				return false;
		} else if (!menus.equals(other.menus))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MenuModel [menus=" + menus + "]";
	}

}
