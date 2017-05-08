package xy.reflect.ui.info.menu.builtin;

import xy.reflect.ui.info.menu.AbstractActionMenuItem;

public abstract class AbstractBuiltInActionMenuItem extends AbstractActionMenuItem {

	private static final long serialVersionUID = 1L;

	public abstract void execute(Object form, Object renderer);

	public boolean isEnabled(Object object, Object renderer) {
		return true;
	}

}
