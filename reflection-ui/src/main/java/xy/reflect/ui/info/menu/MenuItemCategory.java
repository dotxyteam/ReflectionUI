


package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a menu item category. It is commonly displayed as a
 * group of menu items separated from the others by a horizontal bar.
 * 
 * @author olitank
 *
 */
public class MenuItemCategory extends AbstractMenuElementInfo implements IMenuItemContainerInfo {

	protected String caption = "";
	protected List<AbstractMenuItemInfo> items = new ArrayList<AbstractMenuItemInfo>();

	public MenuItemCategory(String caption) {
		this.caption = caption;
	}

	public MenuItemCategory() {
	}

	@Override
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	@Override
	public List<AbstractMenuItemInfo> getItems() {
		return items;
	}

	public void setItems(List<AbstractMenuItemInfo> items) {
		this.items = items;
	}

	public void addItem(AbstractMenuItemInfo item) {
		this.items.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((caption == null) ? 0 : caption.hashCode());
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
		MenuItemCategory other = (MenuItemCategory) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (caption == null) {
			if (other.caption != null)
				return false;
		} else if (!caption.equals(other.caption))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MenuItemCategory [name=" + caption + "]";
	}

}
