package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.ResourcePath;

public abstract class AbstractMenuItem extends AbstractMenuElement {
	private static final long serialVersionUID = 1L;

	protected String name = "";
	protected ResourcePath iconImagePath;

	public AbstractMenuItem(String name, ResourcePath iconImagePath) {
		super();
		this.name = name;
		this.iconImagePath = iconImagePath;
	}

	public AbstractMenuItem() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourcePath getIconImagePath() {
		return iconImagePath;
	}

	public void setIconImagePath(ResourcePath iconImagePath) {
		this.iconImagePath = iconImagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AbstractMenuItem other = (AbstractMenuItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
