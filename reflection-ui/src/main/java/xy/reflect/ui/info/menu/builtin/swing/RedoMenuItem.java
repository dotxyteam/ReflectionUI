package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class RedoMenuItem extends AbstractBuiltInActionMenuItem {

	public RedoMenuItem() {
		name = "Redo";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return ((SwingRenderer) renderer).getModificationStackByForm().get(form).canRedo();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((SwingRenderer) renderer).getModificationStackByForm().get(form).redo();
	}

}
