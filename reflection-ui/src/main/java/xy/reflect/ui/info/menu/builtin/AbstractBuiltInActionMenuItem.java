package xy.reflect.ui.info.menu.builtin;

import xy.reflect.ui.info.menu.AbstractActionMenuItem;

public abstract class AbstractBuiltInActionMenuItem extends AbstractActionMenuItem {

	public abstract void execute(Object form, Object renderer);

	public boolean isEnabled(Object object, Object renderer) {
		return true;
	}

	public String getName(final Object form, final Object renderer) {
		return getName();
	}

}
