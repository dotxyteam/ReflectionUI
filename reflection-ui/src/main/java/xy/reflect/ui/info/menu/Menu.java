package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class Menu extends AbstractMenuItem implements IMenuItemContainer {

	public Menu(String name) {
		super(name);
	}
	
	public Menu() {
		this("");
	}

	private static final long serialVersionUID = 1L;

	protected List<AbstractMenuItem> items = new ArrayList<AbstractMenuItem>();
	protected List<MenuItemCategory> itemCategories = new ArrayList<MenuItemCategory>();

	@Override
	@XmlElements({ @XmlElement(name = "menu", type = Menu.class) })
	public List<AbstractMenuItem> getItems() {
		return items;
	}

	public void setItems(List<AbstractMenuItem> items) {
		this.items = items;
	}

	public void addItem(AbstractMenuItem item) {
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



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((itemCategories == null) ? 0 : itemCategories.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
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
		Menu other = (Menu) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "Menu [name=" + name + "]";
	}

}