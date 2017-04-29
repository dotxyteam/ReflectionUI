package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultMenuElementPosition implements IMenuElementPosition {

	protected List<IMenuItemContainer> elementAncestors;
	protected String elementName;
	protected MenuElementKind elementKind;

	public DefaultMenuElementPosition(String elementName, MenuElementKind elementKind,
			List<IMenuItemContainer> elementAncestors) {
		this.elementName = elementName;
		this.elementKind = elementKind;
		this.elementAncestors = elementAncestors;
	}

	@Override
	public IMenuElementPosition getParent() {
		if(elementAncestors.size() == 0){
			return null;
		}
		IMenuItemContainer parentElement = elementAncestors.get(0);
		List<IMenuItemContainer> parentElementAncestors = new ArrayList<IMenuItemContainer>(elementAncestors);
		parentElementAncestors.remove(0);
		return new DefaultMenuElementPosition(parentElement.getName(),
				ReflectionUIUtils.getMenuElementKind(parentElement), parentElementAncestors);
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	@Override
	public MenuElementKind getElementKind() {
		return elementKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementAncestors == null) ? 0 : elementAncestors.hashCode());
		result = prime * result + ((elementKind == null) ? 0 : elementKind.hashCode());
		result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
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
		DefaultMenuElementPosition other = (DefaultMenuElementPosition) obj;
		if (elementAncestors == null) {
			if (other.elementAncestors != null)
				return false;
		} else if (!elementAncestors.equals(other.elementAncestors))
			return false;
		if (elementKind != other.elementKind)
			return false;
		if (elementName == null) {
			if (other.elementName != null)
				return false;
		} else if (!elementName.equals(other.elementName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMenuElementPosition [elementName=" + elementName + ", elementKind=" + elementKind
				+ ", elementAncestors=" + elementAncestors + "]";
	}

}
