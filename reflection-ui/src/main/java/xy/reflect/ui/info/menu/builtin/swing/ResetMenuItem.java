package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;

public class ResetMenuItem extends AbstractBuiltInActionMenuItem {

	private static final long serialVersionUID = 1L;

	public ResetMenuItem() {
		name = "Reset";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		return((SwingRenderer) renderer).getModificationStackByForm().get(form).canReset();
	}

	@Override
	public void execute(Object form, Object renderer) {
		((SwingRenderer) renderer).getModificationStackByForm().get(form).undoAll();
	}

}
