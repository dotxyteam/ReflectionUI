


package xy.reflect.ui.info.menu;

/**
 * Default implementation of {@link IMenuElementPosition}.
 * 
 * @author olitank
 *
 */
public class DefaultMenuElementPosition implements IMenuElementPosition {

	protected DefaultMenuElementPosition parent;
	protected String elementName;
	protected MenuElementKind elementKind;

	public DefaultMenuElementPosition(String elementName, MenuElementKind elementKind,
			DefaultMenuElementPosition parent) {
		this.elementName = elementName;
		this.elementKind = elementKind;
		this.parent = parent;
	}

	public DefaultMenuElementPosition getRoot() {
		DefaultMenuElementPosition result = this;
		while (result.getParent() != null) {
			result = result.getParent();
		}
		return result;
	}

	@Override
	public DefaultMenuElementPosition getParent() {
		return parent;
	}

	public void setParent(DefaultMenuElementPosition parent) {
		this.parent = parent;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	@Override
	public MenuElementKind getElementKind() {
		return elementKind;
	}

	public void setElementKind(MenuElementKind elementKind) {
		this.elementKind = elementKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
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
		return "DefaultMenuElementPosition [elementName=" + elementName + ", elementKind=" + elementKind + "]";
	}

}
