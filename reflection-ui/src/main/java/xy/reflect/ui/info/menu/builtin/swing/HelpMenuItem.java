package xy.reflect.ui.info.menu.builtin.swing;

import java.awt.Image;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class HelpMenuItem extends AbstractBuiltInActionMenuItem {

	private static final long serialVersionUID = 1L;

	public HelpMenuItem() {
		name = "Help";
	}

	@Override
	public void execute(Object object, Object renderer) {
		SwingRenderer swingRenderer = (SwingRenderer) renderer;
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		String onlineHelp = type.getOnlineHelp();
		if (onlineHelp == null) {
			throw new ReflectionUIError("Online help not provided for the type '" + type.getName() + "'");
		}
		String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), name);
		Image iconImage = swingRenderer.getObjectIconImage(object);
		JPanel form = SwingRendererUtils.findFirstObjectActiveForm(object, swingRenderer);
		swingRenderer.openInformationDialog(form, onlineHelp, title, iconImage);
	}

}
