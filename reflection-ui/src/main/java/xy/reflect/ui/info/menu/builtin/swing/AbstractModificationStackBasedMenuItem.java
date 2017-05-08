package xy.reflect.ui.info.menu.builtin.swing;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractModificationStackBasedMenuItem extends AbstractBuiltInActionMenuItem {

	private static final long serialVersionUID = 1L;

	protected ModificationStack getModificationStack(Object object, SwingRenderer swingRenderer) {
		JPanel form = SwingRendererUtils.findFirstObjectActiveForm(object, swingRenderer);
		return swingRenderer.getModificationStackByForm().get(form);
	}

}