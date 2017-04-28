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

	public IMenuElement contribute(IMenuElementPosition position, IMenuElement element) {
		if (position.getParent() == null) {
			return addOrMergeIn(element, null);
		} else {
			IMenuElementPosition containerPosition = position.getParent();
			IMenuItemContainer container = getContainer(containerPosition);
			return addOrMergeIn(element, container);
		}
	}

	public void merge(MenuModel model) {
		for(Menu menu: model.getMenus()){
			addOrMergeIn(menu);
		}
	}

	public void addOrMergeIn(Menu menu) {
		addOrMergeIn(menu, null);		
	}

	protected void merge(IMenuElement sourceElement, IMenuElement targetElement) {
		if (!same(sourceElement, targetElement)) {
			throw new ReflectionUIError();
		}
		if (!(sourceElement instanceof IMenuItemContainer)) {
			throw new ReflectionUIError(
					"Duplicate menu item detected (cannot merge): '" + sourceElement.getName() + "'");
		}
		for (IMenuElement sourceElementChild : getChildren(sourceElement)) {
			addOrMergeIn(sourceElementChild, (IMenuItemContainer) targetElement);
		}
	}

	protected IMenuItemContainer getContainer(IMenuElementPosition containerPosition) {
		IMenuItemContainer container = createContainer(containerPosition);
		container = (IMenuItemContainer) contribute(containerPosition, container);
		return container;
	}

	protected List<IMenuElement> getChildren(IMenuElement element) {
		List<IMenuElement> result = new ArrayList<IMenuElement>();
		if (element == null) {
			result.addAll(menus);
		}
		if (element instanceof IMenuItemContainer) {
			result.addAll(((IMenuItemContainer) element).getItems());
		}
		if (element instanceof Menu) {
			result.addAll(((Menu) element).getItemCategories());
		}
		return result;
	}

	protected IMenuElement addOrMergeIn(IMenuElement element, IMenuItemContainer container) {
		for (IMenuElement containerChild : getChildren(container)) {
			if (same(element, containerChild)) {
				merge(element, containerChild);
				return containerChild;
			}
		}
		if (container == null) {
			if (!(element instanceof Menu)) {
				throw new ReflectionUIError("Unexpected element at root position: " + element + ". Only "
						+ Menu.class.getSimpleName() + "s are expected at this position");
			}
			menus.add((Menu) element);
		} else if (container instanceof Menu) {
			if (element instanceof AbstractMenuItem) {
				((Menu) container).addItem((AbstractMenuItem) element);
			} else if (element instanceof MenuItemCategory) {
				((Menu) container).addItemCategory((MenuItemCategory) element);
			} else {
				throw new ReflectionUIError();
			}
		} else if (container instanceof MenuItemCategory) {
			if (element instanceof AbstractMenuItem) {
				((MenuItemCategory) container).addItem((AbstractMenuItem) element);
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
		return element;
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
