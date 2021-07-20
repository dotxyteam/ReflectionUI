/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MethodActionMenuItemInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Menu item that allows to invoke a method.
 * 
 * @author olitank
 *
 */
public class MethodActionMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Form form;
	protected MethodActionMenuItemInfo menuItemInfo;

	public MethodActionMenuItem(SwingRenderer swingRenderer, Form form, MethodActionMenuItemInfo menuItemInfo) {
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.menuItemInfo = menuItemInfo;
		initialize();
	}

	protected void initialize() {
		setAction(createAction());
		try {
			setText(menuItemInfo.getCaption());
			ImageIcon icon = swingRenderer.getMenuItemIcon(menuItemInfo);
			if (icon != null) {
				icon = SwingRendererUtils.getSmallIcon(icon);
			}
			setIcon(icon);
			setEnabled(menuItemInfo.getMethod().isEnabled(form.getObject()));
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			if (getText() == null) {
				setText(t.toString());
			} else {
				setText(getText() + "(" + t.toString() + ")");
			}
			setEnabled(false);
		}
	}

	protected Action createAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					IMethodInfo method = menuItemInfo.getMethod();
					IMethodControlInput input = form.createMethodControlPlaceHolder(method);
					MethodAction methodAction = swingRenderer.createMethodAction(input);
					methodAction.onInvocationRequest((Form) form);
				} catch (Throwable t) {
					swingRenderer.handleObjectException(form, t);
				}
			}

		};
	}

}
