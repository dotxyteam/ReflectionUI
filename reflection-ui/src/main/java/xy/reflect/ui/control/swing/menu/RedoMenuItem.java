
package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Menu item that allows to call {@link ModificationStack#redo()} on the current
 * modification stack.
 * 
 * @author olitank
 *
 */
public class RedoMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	public RedoMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
	}

	@Override
	protected void configure() {
		super.configure();
		IModification nextRedoModification = getContextForm().getModificationStack().getNextRedoModification();
		if (nextRedoModification != null) {
			setToolTipText(swingRenderer.prepareMessageToDisplay(nextRedoModification.getTitle()));
		} else {
			setToolTipText(null);
		}
	}

	@Override
	protected boolean isActive() {
		return getContextForm().getModificationStack().canRedo();
	}

	@Override
	protected void execute() {
		getContextForm().getModificationStack().redo();
	}

}
