


package xy.reflect.ui.control.swing.menu;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Menu item that allows to call {@link ModificationStack#undo()} on the current
 * modification stack.
 * 
 * @author olitank
 *
 */
public class UndoMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	public UndoMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
	}

	@Override
	protected void initialize() {
		super.initialize();
		IModification nextUndoModification = form.getModificationStack().getNextUndoModification();
		if (nextUndoModification != null) {
			setToolTipText(nextUndoModification.getTitle());
		} else {
			setToolTipText(null);
		}
	}

	@Override
	protected boolean isActive() {
		return form.getModificationStack().canUndo();
	}

	@Override
	protected void execute() {
		form.getModificationStack().undo();
	}

}
