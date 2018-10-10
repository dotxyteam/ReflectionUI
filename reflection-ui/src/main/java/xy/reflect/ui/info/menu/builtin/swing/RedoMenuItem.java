package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class RedoMenuItem extends AbstractBuiltInActionMenuItem {

	public RedoMenuItem() {
		name = "Redo";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return ((Form) form).getModificationStack().canRedo();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((Form) form).getModificationStack().redo();
	}

}
