package xy.reflect.ui.info.menu;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import xy.reflect.ui.info.menu.builtin.ExitMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.HelpMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.RedoMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.ResetMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.UndoMenuItem;

public class MenuItemCategory extends AbstractMenuElement implements IMenuItemContainer {
	private static final long serialVersionUID = 1L;

	protected String name = "";
	protected List<AbstractMenuItem> items = new ArrayList<AbstractMenuItem>();

	public MenuItemCategory(String name) {
		this.name = name;
	}

	public MenuItemCategory() {
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@XmlElements({ @XmlElement(name = "menu", type = Menu.class),
			@XmlElement(name = "exitMenuItem", type = ExitMenuItem.class),
			@XmlElement(name = "helpMenuItem", type = HelpMenuItem.class),
			@XmlElement(name = "undoMenuItem", type = UndoMenuItem.class),
			@XmlElement(name = "redoMenuItem", type = RedoMenuItem.class),
			@XmlElement(name = "resetMenuItem", type = ResetMenuItem.class) })
	public List<AbstractMenuItem> getItems() {
		return items;
	}

	public void setItems(List<AbstractMenuItem> items) {
		this.items = items;
	}

	public void addItem(AbstractMenuItem item) {
		this.items.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MenuItemCategory [name=" + name + "]";
	}

}