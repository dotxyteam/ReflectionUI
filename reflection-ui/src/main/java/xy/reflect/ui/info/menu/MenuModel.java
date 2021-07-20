


package xy.reflect.ui.info.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.Visitor;

/**
 * This class allows to specify menu elements that will be available in the
 * generated UIs. It also allows to process these elements (merge multiple
 * instances, ...) in order to provide to the UI renderer a structure that is
 * ready for display.
 * 
 * @author olitank
 *
 */
public class MenuModel implements Serializable {

	private static final long serialVersionUID = 1L;

	protected List<MenuInfo> menus = new ArrayList<MenuInfo>();

	public List<MenuInfo> getMenus() {
		return menus;
	}

	public void setMenus(List<MenuInfo> menus) {
		this.menus = menus;
	}

	public void importContribution(IMenuElementPosition containerPosition, IMenuElementInfo element,
			Filter<IMenuElementInfo> addedElementFilter) {
		if (containerPosition == null) {
			importContributionIn(element, null, addedElementFilter);
		} else {
			if ((containerPosition.getElementKind() != MenuElementKind.ITEM_CATEGORY)
					&& (containerPosition.getElementKind() != MenuElementKind.MENU)) {
				throw new ReflectionUIError("Failed to add menu contribution '" + element + "' in '" + containerPosition
						+ "': Invalid container");
			}
			IMenuItemContainerInfo container = (IMenuItemContainerInfo) findElement(containerPosition);
			if (container == null) {
				throw new ReflectionUIError("Failed to add menu contribution '" + element + "' in '" + containerPosition
						+ "': Container not found: '" + containerPosition + "'");
			}
			importContributionIn(element, container, addedElementFilter);
		}
	}

	public void importContribution(IMenuElementPosition containerPosition, IMenuElementInfo element) {
		importContribution(containerPosition, element, Filter.nullFilter());
	}

	public void importContributions(MenuModel model, Filter<IMenuElementInfo> addedElementFilter) {
		for (MenuInfo menu : model.getMenus()) {
			importContributionIn(menu, null, addedElementFilter);
		}
	}

	public void importContributions(MenuModel model) {
		importContributions(model, Filter.nullFilter());
	}

	public void visit(Visitor<IMenuElementInfo> visitor) {
		visitChildren(visitor, null);
	}

	protected boolean visitChildren(Visitor<IMenuElementInfo> visitor, IMenuElementInfo element) {
		for (IMenuElementInfo child : getChildren(element)) {
			if (!visitor.visit(child)) {
				return false;
			}
			if (!visitChildren(visitor, child)) {
				return false;
			}
		}
		return true;
	}

	protected void importChildrenContributions(IMenuItemContainerInfo sourceContainer,
			IMenuItemContainerInfo targetContainer, Filter<IMenuElementInfo> addedElementFilter) {
		if (!same(sourceContainer, targetContainer)) {
			throw new ReflectionUIError();
		}
		for (IMenuElementInfo sourceElementChild : getChildren(sourceContainer)) {
			importContributionIn(sourceElementChild, (IMenuItemContainerInfo) targetContainer, addedElementFilter);
		}
	}

	protected IMenuElementInfo findElement(IMenuElementPosition elementPosition) {
		if (elementPosition.getParent() == null) {
			if (elementPosition.getElementKind() != MenuElementKind.MENU) {
				throw new ReflectionUIError("Illegal root elemnt '" + elementPosition + "'. Root element kind must be '"
						+ MenuElementKind.MENU + "'");
			}
		}
		List<IMenuElementPosition> ancestorPositions = ReflectionUIUtils.getAncestors(elementPosition);
		ancestorPositions = new ArrayList<IMenuElementPosition>(ancestorPositions);
		Collections.reverse(ancestorPositions);
		IMenuItemContainerInfo container = null;
		for (IMenuElementPosition ancestorPosition : ancestorPositions) {
			container = (IMenuItemContainerInfo) findChildElement(container, ancestorPosition.getElementKind(),
					ancestorPosition.getElementName());
			if (container == null) {
				return null;
			}
		}
		IMenuElementInfo result = findChildElement(container, elementPosition.getElementKind(),
				elementPosition.getElementName());
		return result;
	}

	protected IMenuElementInfo findChildElement(IMenuItemContainerInfo container, MenuElementKind childElementKind,
			String childElementName) {
		for (IMenuElementInfo childElement : getChildren(container)) {
			if (ReflectionUIUtils.getMenuElementKind(childElement) == childElementKind) {
				if (childElement.getCaption().equals(childElementName)) {
					return childElement;
				}
			}
		}
		return null;
	}

	protected List<IMenuElementInfo> getChildren(IMenuElementInfo element) {
		List<IMenuElementInfo> result = new ArrayList<IMenuElementInfo>();
		if (element == null) {
			result.addAll(menus);
		}
		if (element instanceof IMenuItemContainerInfo) {
			result.addAll(((IMenuItemContainerInfo) element).getItems());
		}
		if (element instanceof MenuInfo) {
			result.addAll(((MenuInfo) element).getItemCategories());
		}
		return result;
	}

	protected void importContributionIn(IMenuElementInfo element, IMenuItemContainerInfo container,
			Filter<IMenuElementInfo> addedElementFilter) {
		for (IMenuElementInfo containerChild : getChildren(container)) {
			if (same(element, containerChild)) {
				if (!(containerChild instanceof IMenuItemContainerInfo)) {
					final String errorMenuName = "<MENU ERROR> Duplicate menu detected: " + element.getCaption()
							+ " (id=" + element.hashCode() + ")";
					element = new AbstractActionMenuItemInfo(errorMenuName, null) {

					};
					importContributionIn(element, container, addedElementFilter);
					return;
				}
				importChildrenContributions((IMenuItemContainerInfo) element, (IMenuItemContainerInfo) containerChild,
						addedElementFilter);
				return;
			}
		}
		if (element instanceof IMenuItemContainerInfo) {
			IMenuItemContainerInfo sameElement = createSameContainer((IMenuItemContainerInfo) element);
			importChildrenContributions((IMenuItemContainerInfo) element, sameElement, addedElementFilter);
			element = sameElement;
		}
		if (container == null) {
			if (!(element instanceof MenuInfo)) {
				throw new ReflectionUIError("Unexpected element at root position: " + element + ". Only "
						+ MenuInfo.class.getSimpleName() + "s are expected at this position");
			}
			menus.add((MenuInfo) addedElementFilter.get(element));
		} else if (container instanceof MenuInfo) {
			if (element instanceof AbstractMenuItemInfo) {
				((MenuInfo) container).addItem((AbstractMenuItemInfo) addedElementFilter.get(element));
			} else if (element instanceof MenuItemCategory) {
				((MenuInfo) container).addItemCategory((MenuItemCategory) addedElementFilter.get(element));
			} else {
				throw new ReflectionUIError();
			}
		} else if (container instanceof MenuItemCategory) {
			if (element instanceof AbstractMenuItemInfo) {
				((MenuItemCategory) container).addItem((AbstractMenuItemInfo) addedElementFilter.get(element));
			} else {
				throw new ReflectionUIError();
			}
		} else {
			throw new ReflectionUIError();
		}
	}

	protected IMenuItemContainerInfo createSameContainer(IMenuItemContainerInfo element) {
		if (element instanceof MenuInfo) {
			return new MenuInfo(((MenuInfo) element).getCaption());
		} else if (element instanceof MenuItemCategory) {
			return new MenuItemCategory(((MenuItemCategory) element).getCaption());
		} else {
			throw new ReflectionUIError();
		}
	}

	protected boolean same(IMenuElementInfo menuElement1, IMenuElementInfo menuElement2) {
		if (ReflectionUIUtils.getMenuElementKind(menuElement1) == ReflectionUIUtils.getMenuElementKind(menuElement2)) {
			if (menuElement1.getCaption().equals(menuElement2.getCaption())) {
				return true;
			}
		}
		return false;
	}

	protected IMenuItemContainerInfo createContainer(IMenuElementPosition containerPosition) {
		if (containerPosition.getElementKind() == MenuElementKind.MENU) {
			return new MenuInfo(containerPosition.getElementName());
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
