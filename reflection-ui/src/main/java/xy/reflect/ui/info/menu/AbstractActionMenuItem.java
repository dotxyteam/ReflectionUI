package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.ResourcePath;

public abstract class AbstractActionMenuItem extends AbstractMenuItem {

	protected ResourcePath iconImagePath;

	public AbstractActionMenuItem(String name, ResourcePath iconImagePath) {
		super(name);
		this.iconImagePath = iconImagePath;
	}

	public AbstractActionMenuItem() {
		super();
	}

	public ResourcePath getIconImagePath() {
		return iconImagePath;
	}

	public void setIconImagePath(ResourcePath iconImagePath) {
		this.iconImagePath = iconImagePath;
	}

}