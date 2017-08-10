package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class UndoMenuItem extends AbstractBuiltInActionMenuItem {

	public UndoMenuItem() {
		name = "Undo";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return ((SwingRenderer) renderer).getModificationStackByForm().get(form).canUndo();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((SwingRenderer) renderer).getModificationStackByForm().get(form).undo();

	}

}
