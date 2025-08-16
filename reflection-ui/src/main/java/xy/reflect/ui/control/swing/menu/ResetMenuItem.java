
package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
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

	public ResetMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandardActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
	}

	@Override
	protected boolean isActive() {
		return getContextForm().getModificationStack().canReset();
	}

	@Override
	protected void execute() {
		getContextForm().getModificationStack().undoAll();
	}

}
