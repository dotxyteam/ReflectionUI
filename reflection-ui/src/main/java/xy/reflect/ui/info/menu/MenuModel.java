package xy.reflect.ui.info.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.Visitor;

public class MenuModel implements Serializable {

	private static final long serialVersionUID = 1L;
	protected List<Menu> menus = new ArrayList<Menu>();

	public List<Menu> getMenus() {
		return menus;
	}

	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}

	public IMenuElement importContribution(IMenuElementPosition containerPosition, IMenuElement element) {
		if (containerPosition == null) {
			return importContributionIn(element, null);
		} else {
			if ((containerPosition.getElementKind() != MenuElementKind.ITEM_CATEGORY)
					&& (containerPosition.getElementKind() != MenuElementKind.MENU)) {
				throw new ReflectionUIError("Failed to add menu contribution '" + element + "' in '" + containerPosition
						+ "': Invalid container");
			}
			IMenuItemContainer container = (IMenuItemContainer) findElement(containerPosition);
			if (container == null) {
				throw new ReflectionUIError("Failed to add menu contribution '" + element + "' in '" + containerPosition
						+ "': Container not found: '" + containerPosition + "'");
			}
			return importContributionIn(element, container);
		}
	}

	public void importContributions(MenuModel model) {
		for (Menu menu : model.getMenus()) {
			importContributionIn(menu, null);
		}
	}

	public void visit(Visitor<IMenuElement> visitor) {
		visitChildren(visitor, null);
	}

	protected boolean visitChildren(Visitor<IMenuElement> visitor, IMenuElement element) {
		for (IMenuElement child : getChildren(element)) {
			if (!visitor.visit(child)) {
				return false;
			}
			if (!visitChildren(visitor, child)) {
				return false;
			}
		}
		return true;
	}

	protected void importChildrenContributions(IMenuItemContainer sourceContainer, IMenuItemContainer targetContainer) {
		if (!same(sourceContainer, targetContainer)) {
			throw new ReflectionUIError();
		}
		for (IMenuElement sourceElementChild : getChildren(sourceContainer)) {
			importContributionIn(sourceElementChild, (IMenuItemContainer) targetContainer);
		}
	}

	protected IMenuElement findElement(IMenuElementPosition elementPosition) {
		if (elementPosition.getParent() == null) {
			if (elementPosition.getElementKind() != MenuElementKind.MENU) {
				throw new ReflectionUIError("Illegal root elemnt '" + elementPosition + "'. Root element kind must be '"
						+ MenuElementKind.MENU + "'");
			}
		}
		List<IMenuElementPosition> ancestorPositions = ReflectionUIUtils.getAncestors(elementPosition);
		ancestorPositions = new ArrayList<IMenuElementPosition>(ancestorPositions);
		Collections.reverse(ancestorPositions);
		IMenuItemContainer container = null;
		for (IMenuElementPosition ancestorPosition : ancestorPositions) {
			container = (IMenuItemContainer) findChildElement(container, ancestorPosition.getElementKind(),
					ancestorPosition.getElementName());
			if (container == null) {
				return null;
			}
		}
		IMenuElement result = findChildElement(container, elementPosition.getElementKind(),
				elementPosition.getElementName());
		return result;
	}

	protected IMenuElement findChildElement(IMenuItemContainer container, MenuElementKind childElementKind,
			String childElementName) {
		for (IMenuElement childElement : getChildren(container)) {
			if (ReflectionUIUtils.getMenuElementKind(childElement) == childElementKind) {
				if (childElement.getName().equals(childElementName)) {
					return childElement;
				}
			}
		}
		return null;
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

	protected IMenuElement importContributionIn(IMenuElement element, IMenuItemContainer container) {
		for (IMenuElement containerChild : getChildren(container)) {
			if (same(element, containerChild)) {
				if (!(containerChild instanceof IMenuItemContainer)) {
					throw new ReflectionUIError(
							"Duplicate menu item detected (cannot merge): '" + containerChild.getName() + "'");
				}
				importChildrenContributions((IMenuItemContainer) element, (IMenuItemContainer) containerChild);
				return containerChild;
			}
		}
		if (element instanceof IMenuItemContainer) {
			IMenuItemContainer sameElement = createSameContainer((IMenuItemContainer) element);
			importChildrenContributions((IMenuItemContainer) element, sameElement);
			element = sameElement;
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

	protected IMenuItemContainer createSameContainer(IMenuItemContainer element) {
		if (element instanceof Menu) {
			return new Menu(((Menu) element).getName());
		} else if (element instanceof MenuItemCategory) {
			return new MenuItemCategory(((MenuItemCategory) element).getName());
		} else {
			throw new ReflectionUIError();
		}
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
