package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class UndoMenuItem extends AbstractBuiltInActionMenuItem {

	public UndoMenuItem() {
		name = "Undo";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return ((Form) form).getModificationStack().canUndo();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((Form) form).getModificationStack().undo();

	}

}
