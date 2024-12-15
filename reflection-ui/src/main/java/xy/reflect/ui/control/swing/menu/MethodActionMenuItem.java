/*
 * 
 */
package xy.reflect.ui.control.swing.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

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
import xy.reflect.ui.util.ReflectionUIUtils;

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
		customizeUI();
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

	protected void customizeUI() {
		Color awtBackgroundColor = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getMainBackgroundColor() != null)
						? SwingRendererUtils
								.getColor(swingRenderer.getReflectionUI().getApplicationInfo().getMainBackgroundColor())
						: null;
		Color awtForegroundColor = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getMainForegroundColor() != null)
						? SwingRendererUtils
								.getColor(swingRenderer.getReflectionUI().getApplicationInfo().getMainForegroundColor())
						: null;
		Font labelCustomFont = (swingRenderer.getReflectionUI().getApplicationInfo()
				.getLabelCustomFontResourcePath() != null)
						? SwingRendererUtils
								.loadFontThroughCache(
										swingRenderer.getReflectionUI().getApplicationInfo()
												.getLabelCustomFontResourcePath(),
										ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
								.deriveFont(getFont().getStyle(), getFont().getSize())
						: null;
		if (awtBackgroundColor != null) {
			setBackground(awtBackgroundColor);
		}
		if (awtForegroundColor != null) {
			setForeground(awtForegroundColor);
		}
		if (labelCustomFont != null) {
			setFont(labelCustomFont);
		}

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
							return form.getModificationStack();
						}

						@Override
						public IContext getContext() {
							ITypeInfo objectType = swingRenderer.getReflectionUI()
									.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(form.getObject()));
							return new MethodContext(objectType, menuItemInfo.getMethod());
						}

						@Override
						public IMethodControlData getControlData() {
							return new DefaultMethodControlData(swingRenderer.getReflectionUI(), form.getObject(),
									menuItemInfo.getMethod());
						}
					});
					methodAction.onInvocationRequest((Form) form);
				} catch (Throwable t) {
					swingRenderer.handleObjectException(form, t);
				}
			}

		};
	}

}
