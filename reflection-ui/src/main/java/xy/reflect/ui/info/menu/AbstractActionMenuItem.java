package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.ResourcePath;

public abstract class AbstractActionMenuItem extends AbstractMenuItem {

	protected ResourcePath iconImagePath;

	public abstract void execute(Object form, Object renderer);

	public abstract boolean isEnabled(Object object, Object renderer);

	public abstract String getName(final Object form, final Object renderer);

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