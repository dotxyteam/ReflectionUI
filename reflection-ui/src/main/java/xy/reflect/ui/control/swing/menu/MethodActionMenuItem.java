/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodContext;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MethodActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.KeyboardShortcut;

/**
 * Menu item that allows to invoke a method.
 * 
 * @author olitank
 *
 */
public class MethodActionMenuItem extends AbstractMenuItem {

	private static final long serialVersionUID = 1L;

	protected MethodActionMenuItemInfo menuItemInfo;

	public MethodActionMenuItem(SwingRenderer swingRenderer, Form menuBarOwner, MethodActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner);
		this.menuItemInfo = menuItemInfo;
		configure();
	}

	public Form getContextForm() {
		return (Form) menuItemInfo.getSpecificProperties().get(Form.ACTION_MENU_ITEM_CONTEXT_FORM);
	}

	@Override
	public void refresh() {
		configure();
	}

	protected void configure() {
		setAction(createAction());
		setAccelerator(createAccelerator());
		try {
			setText(menuItemInfo.getCaption());
			Image image = swingRenderer.getMenuItemIconImage(menuItemInfo);
			ImageIcon icon;
			if (image != null) {
				icon = SwingRendererUtils.getSmallIcon(SwingRendererUtils.getIcon(image));
			} else {
				icon = null;
			}
			setIcon(icon);
			setEnabled(menuItemInfo.getMethod().isEnabled(getContextForm().getObject()));
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

	protected KeyStroke createAccelerator() {
		KeyboardShortcut keyboardShortcut = menuItemInfo.getKeyboardShortcut();
		if (keyboardShortcut == null) {
			return null;
		}
		return KeyStroke.getKeyStroke(keyboardShortcut.getKeyCode(), keyboardShortcut.getModifiers());
	}

	protected Action createAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					MethodAction methodAction = swingRenderer.createMethodAction(new IMethodControlInput() {

						@Override
						public ModificationStack getModificationStack() {
							return getContextForm().getModificationStack();
						}

						@Override
						public IContext getContext() {
							ITypeInfo objectType = swingRenderer.getReflectionUI().getTypeInfo(
									swingRenderer.getReflectionUI().getTypeInfoSource(getContextForm().getObject()));
							return new MethodContext(objectType, menuItemInfo.getMethod());
						}

						@Override
						public IMethodControlData getControlData() {
							return new DefaultMethodControlData(swingRenderer.getReflectionUI(),
									getContextForm().getObject(), menuItemInfo.getMethod());
						}
					});
					methodAction.onInvocationRequest(menuBarOwner);
				} catch (Throwable t) {
					swingRenderer.handleException(menuBarOwner, t);
				}
			}

		};
	}

}
