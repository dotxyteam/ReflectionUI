/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractControlButton;

public class DialogBuilder {

	protected SwingRenderer swingRenderer;

	protected String title;
	protected Image iconImage;
	protected Component ownerComponent;
	protected Component contentComponent;
	protected List<Component> buttonBarControls;
	protected Runnable whenClosing;
	protected boolean okPressed = false;
	protected JDialog dialog;
	protected Image buttonBackgroundImage;
	protected Color buttonBackgroundColor;
	protected Color buttonForegroundColor;
	protected Color buttonBorderColor;

	public DialogBuilder(SwingRenderer swingRenderer, Component ownerComponent) {
		super();
		this.ownerComponent = ownerComponent;
		this.swingRenderer = swingRenderer;
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getIconImagePath() != null) {
			this.iconImage = SwingRendererUtils.loadImageThroughCache(appInfo.getIconImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		}
	}

	public Image getButtonBackgroundImage() {
		return buttonBackgroundImage;
	}

	public void setButtonBackgroundImage(Image buttonBackgroundImage) {
		this.buttonBackgroundImage = buttonBackgroundImage;
	}

	public Color getButtonBackgroundColor() {
		return buttonBackgroundColor;
	}

	public void setButtonBackgroundColor(Color buttonBackgroundColor) {
		this.buttonBackgroundColor = buttonBackgroundColor;
	}

	public Color getButtonForegroundColor() {
		return buttonForegroundColor;
	}

	public void setButtonForegroundColor(Color buttonForegroundColor) {
		this.buttonForegroundColor = buttonForegroundColor;
	}

	public Color getButtonBorderColor() {
		return buttonBorderColor;
	}

	public void setButtonBorderColor(Color buttonBorderColor) {
		this.buttonBorderColor = buttonBorderColor;
	}

	public boolean wasOkPressed() {
		return okPressed;
	}

	public JDialog getCreatedDialog() {
		return dialog;
	}

	public Component getOwnerComponent() {
		return ownerComponent;
	}

	public Component getContentComponent() {
		return contentComponent;
	}

	public void setContentComponent(Component contentComponent) {
		this.contentComponent = contentComponent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Image getIconImage() {
		return iconImage;
	}

	public void setIconImage(Image iconImage) {
		this.iconImage = iconImage;
	}

	public List<Component> getButtonBarControls() {
		return buttonBarControls;
	}

	public void setButtonBarControls(List<Component> buttonBarControls) {
		this.buttonBarControls = buttonBarControls;
	}

	public Runnable getWhenClosing() {
		return whenClosing;
	}

	public void setWhenClosing(Runnable whenClosing) {
		this.whenClosing = whenClosing;
	}

	public JButton createDialogClosingButton(final String caption, final Runnable beforeClosingAction) {
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public Image retrieveBackgroundImage() {
				return buttonBackgroundImage;
			}

			@Override
			public Color retrieveBackgroundColor() {
				return buttonBackgroundColor;
			}

			@Override
			public Color retrieveForegroundColor() {
				return buttonForegroundColor;
			}

			@Override
			public Color retrieveBorderColor() {
				return buttonBorderColor;
			}

			@Override
			public String retrieveCaption() {
				return caption;
			}

		};
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (beforeClosingAction != null) {
						beforeClosingAction.run();
					}
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(result, t);
				} finally {
					dialog.dispose();
				}
			}
		});
		return result;
	}

	public List<JButton> createStandardOKCancelDialogButtons(String customOKCaption, String customCancelCaption) {
		List<JButton> result = new ArrayList<JButton>();
		result.add(createDialogClosingButton((customOKCaption == null) ? "OK" : customOKCaption, new Runnable() {
			@Override
			public void run() {
				okPressed = true;
			}
		}));
		result.add(createDialogClosingButton((customCancelCaption == null) ? "Cancel" : customCancelCaption,
				new Runnable() {
					@Override
					public void run() {
						okPressed = false;
					}
				}));
		return result;
	}

	public JDialog createDialog() {
		Window owner = SwingRendererUtils.getWindowAncestorOrSelf(ownerComponent);
		dialog = new JDialog(owner) {
			protected static final long serialVersionUID = 1L;
			protected boolean disposed = false;

			@Override
			public void dispose() {
				if (disposed) {
					return;
				}
				disposed = true;
				super.dispose();
				executeClosingTask();
			}

			private void executeClosingTask() {
				if (whenClosing != null) {
					try {
						whenClosing.run();
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(this, t);
					}
				}
			}

		};
		WindowManager windowManager = swingRenderer.createWindowManager(dialog);
		windowManager.set(contentComponent, buttonBarControls, title, iconImage);
		dialog.setResizable(true);
		return dialog;
	}
}
