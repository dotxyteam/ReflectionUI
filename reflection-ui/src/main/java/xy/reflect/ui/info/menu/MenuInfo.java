
package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.KeyboardKey;

/**
 * This class represents a menu.
 * 
 * @author olitank
 *
 */
public class MenuInfo extends AbstractMenuItemInfo implements IMenuItemContainerInfo {

	public MenuInfo(String name) {
		super(name);
	}

	public MenuInfo() {
	}

	protected List<AbstractMenuItemInfo> items = new ArrayList<AbstractMenuItemInfo>();
	protected List<MenuItemCategory> itemCategories = new ArrayList<MenuItemCategory>();
	protected KeyboardKey mnemonicKey;

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

	public List<MenuItemCategory> getItemCategories() {
		return itemCategories;
	}

	public void setItemCategories(List<MenuItemCategory> itemCategories) {
		this.itemCategories = itemCategories;
	}

	public void addItemCategory(MenuItemCategory itemCategory) {
		this.itemCategories.add(itemCategory);
	}

	public KeyboardKey getMnemonicKey() {
		return mnemonicKey;
	}

	public void setMnemonicKey(KeyboardKey mnemonicKey) {
		this.mnemonicKey = mnemonicKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemCategories == null) ? 0 : itemCategories.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((mnemonicKey == null) ? 0 : mnemonicKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuInfo other = (MenuInfo) obj;
		if (itemCategories == null) {
			if (other.itemCategories != null)
				return false;
		} else if (!itemCategories.equals(other.itemCategories))
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (mnemonicKey == null) {
			if (other.mnemonicKey != null)
				return false;
		} else if (!mnemonicKey.equals(other.mnemonicKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Menu [name=" + caption + "]";
	}

}
