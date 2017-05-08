package xy.reflect.ui.info.menu.builtin.swing;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class UndoMenuItem extends AbstractModificationStackBasedMenuItem {

	private static final long serialVersionUID = 1L;

	public UndoMenuItem() {
		name = "Undo";
	}

	@Override
	public boolean isEnabled(Object object, Object renderer) {
		return getModificationStack(object, (SwingRenderer) renderer).canUndo();
	}

	@Override
	public void execute(Object object, Object renderer) {
		getModificationStack(object, (SwingRenderer) renderer).undo();

	}

}
