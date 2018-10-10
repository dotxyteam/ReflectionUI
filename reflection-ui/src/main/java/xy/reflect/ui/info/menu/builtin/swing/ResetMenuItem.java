package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class ResetMenuItem extends AbstractBuiltInActionMenuItem {

	public ResetMenuItem() {
		name = "Reset";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return((Form) form).getModificationStack().canReset();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((Form) form).getModificationStack().undoAll();
	}

}
