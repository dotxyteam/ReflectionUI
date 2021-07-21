


package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Menu item that allows to call {@link ModificationStack#undoAll()} on the
 * current modification stack.
 * 
 * @author olitank
 *
 */
public class ResetMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	public ResetMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected boolean isActive() {
		return form.getModificationStack().canReset();
	}

	@Override
	protected void execute() {
		form.getModificationStack().undoAll();
	}

}
