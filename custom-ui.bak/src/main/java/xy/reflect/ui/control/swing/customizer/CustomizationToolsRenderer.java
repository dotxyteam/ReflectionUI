/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.control.swing.renderer.WindowManager;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AlternativeWindowDecorationsPanel;

public class CustomizationToolsRenderer extends SwingCustomizer {

	public CustomizationToolsRenderer(CustomizationToolsUI toolsUI) {
		super(toolsUI, MoreSystemProperties.getInfoCustomizationToolsCustomizationsFilePath());
	}

	public CustomizationToolsUI getCustomizationToolsUI() {
		return (CustomizationToolsUI) getCustomizedUI();
	}

	protected Color getToolsForegroundColor() {
		IApplicationInfo toolsAppInfo = getCustomizationToolsUI().getApplicationInfo();
		if (toolsAppInfo.getTitleForegroundColor() != null) {
			return SwingRendererUtils.getColor(toolsAppInfo.getTitleForegroundColor());
		}
		return new Color(0, 255, 255);
	}

	protected Color getToolsBackgroundColor() {
		IApplicationInfo toolsAppInfo = getCustomizationToolsUI().getApplicationInfo();
		if (toolsAppInfo.getTitleBackgroundColor() != null) {
			return SwingRendererUtils.getColor(toolsAppInfo.getTitleBackgroundColor());
		}
		return new Color(0, 0, 0);
	}

	@Override
	public WindowManager createWindowManager(Window window) {
		return new WindowManager(this, window) {

			@Override
			protected void layoutContentPane(Container contentPane) {
				alternativeDecorationsPanel = createAlternativeWindowDecorationsPanel(window, contentPane);
				rootPane.add(alternativeDecorationsPanel, StackLayout.TOP);
			}

			protected AlternativeWindowDecorationsPanel createAlternativeWindowDecorationsPanel(final Window window,
					final Component windowContent) {
				String title = SwingRendererUtils.getWindowTitle(window);
				Image iconImage = window.getIconImages().get(0);
				ImageIcon icon;
				if (SwingRendererUtils.isNullImage(iconImage)) {
					icon = null;
				} else {
					icon = SwingRendererUtils.getSmallIcon(new ImageIcon(iconImage));
				}
				return new CustomWindowDecorationsPanel(title, icon, window, windowContent) {

					private static final long serialVersionUID = 1L;

					@Override
					public void configureWindow(Window window) {
						super.configureWindow(window);
						if (window instanceof JFrame) {
							getCloseButton().setVisible(false);
							getMaximizeButton().setVisible(false);
							((JFrame) window).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						}
					}

				};
			}

		};
	}

}
