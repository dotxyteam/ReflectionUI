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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.swing.AlternativeWindowDecorationsPanel;
import xy.reflect.ui.util.swing.ControlPanel;
import xy.reflect.ui.util.swing.ControlScrollPane;
import xy.reflect.ui.util.swing.ImagePanel;
import xy.reflect.ui.util.swing.ScrollPaneOptions;

public class WindowManager {

	protected SwingRenderer swingRenderer;
	protected Window window;
	protected AlternativeWindowDecorationsPanel alternativeDecorationsPanel;
	protected ControlPanel rootPane;
	protected ImagePanel backgroundPane;
	protected ControlPanel contentPane;
	protected ControlPanel topBarsContainer;
	protected JScrollPane scrollPane;
	protected JPanel buttonBar;
	protected List<Component> buttonBarControls;

	public WindowManager(SwingRenderer swingRenderer, Window window) {
		this.swingRenderer = swingRenderer;
		this.window = window;
	}

	protected ControlPanel createRootPane() {
		ControlPanel result = new ControlPanel();
		result.setLayout(new StackLayout());
		return result;
	}

	protected ImagePanel createBackgroundPane() {
		ImagePanel result = new ImagePanel();
		result.setPreservingRatio(true);
		result.setFillingAreaWhenPreservingRatio(true);
		return result;
	}

	protected AlternativeWindowDecorationsPanel createAlternativeWindowDecorationsPanel(Window window,
			Component windowContent) {
		String title = SwingRendererUtils.getWindowTitle(window);
		Image iconImage = window.getIconImages().get(0);
		ImageIcon icon;
		if (SwingRendererUtils.isNullImage(iconImage)) {
			icon = null;
		} else {
			icon = SwingRendererUtils.getSmallIcon(new ImageIcon(iconImage));
		}
		return new CustomWindowDecorationsPanel(title, icon, window, windowContent);
	}

	protected Color getAlternativeDecorationsBorderColor() {
		Color result = getMainForegroundColor();
		if (result == null) {
			result = UIManager.getColor("Panel.foreground");
		}
		return result;
	}

	protected JScrollPane createScrollPane(Component content) {
		ControlScrollPane result = new ControlScrollPane(new ScrollPaneOptions(content, true, false));
		result.setBorder(BorderFactory.createEmptyBorder());
		return result;
	}

	protected ControlPanel createContentPane() {
		ControlPanel result = new ControlPanel();
		result.setLayout(new BorderLayout());
		topBarsContainer = new ControlPanel();
		{
			topBarsContainer.setLayout(new BorderLayout());
			result.add(topBarsContainer, BorderLayout.NORTH);
		}
		return result;
	}

	protected JPanel createButtonBar() {
		JPanel result = new ControlPanel();
		result.setLayout(new FlowLayout(FlowLayout.CENTER));
		if ((buttonBarControls != null) && (buttonBarControls.size() > 0)) {
			result.setVisible(true);
			for (Component tool : buttonBarControls) {
				result.add(tool);
			}
		} else {
			result.setVisible(false);
		}
		return result;
	}

	protected void layoutRootPane(ControlPanel rootPane) {
		SwingRendererUtils.setContentPane(window, rootPane);
	}

	protected void layoutBackgroundPane(ImagePanel backgroundPane) {
		rootPane.add(backgroundPane, StackLayout.BOTTOM);
	}

	protected void layoutContentPane(Container contentPane) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (!appInfo.isSystemIntegrationCrossPlatform()) {
			alternativeDecorationsPanel = null;
			SwingRendererUtils.setUndecorated(window, false);
			rootPane.add(contentPane, StackLayout.TOP);
		} else {
			alternativeDecorationsPanel = createAlternativeWindowDecorationsPanel(window, contentPane);
			rootPane.add(alternativeDecorationsPanel, StackLayout.TOP);
		}
	}

	protected void layoutMenuBar(JMenuBar menuBar) {
		topBarsContainer.add(menuBar, BorderLayout.NORTH);
	}

	protected void layoutStatusBar(Component statusBar) {
		topBarsContainer.add(statusBar, BorderLayout.SOUTH);
	}

	protected void layoutContent(Component content) {
		contentPane.add(content, BorderLayout.CENTER);
	}

	protected void layoutButtonBar(JPanel buttonBar) {
		contentPane.add(buttonBar, BorderLayout.SOUTH);
	}

	public void set(final Component content, List<Component> buttonBarControls, String title, Image iconImage) {
		setTitle(title);
		setIconImage(iconImage);
		set(content, buttonBarControls);
	}

	public void adjustBounds() {
		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public void set(Component content, List<Component> buttonBarControls) {
		this.buttonBarControls = buttonBarControls;
		rootPane = createRootPane();
		layoutRootPane(rootPane);
		backgroundPane = createBackgroundPane();
		layoutBackgroundPane(backgroundPane);
		contentPane = createContentPane();
		layoutContentPane(contentPane);
		if (content != null) {
			if (SwingRendererUtils.isForm(content, swingRenderer)) {
				final Form form = (Form) content;
				layoutMenuBar(form.getMenuBar());
				layoutStatusBar(form.getStatusBar());
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowOpened(WindowEvent e) {
						form.updateMenuBar();
						form.validateFormInBackgroundAndReportOnStatusBar();
						SwingRendererUtils.requestAnyComponentFocus(form, swingRenderer);
					}
				});
				form.getRefreshListeners().add(new Form.IRefreshListener() {
					@Override
					public void onRefresh(boolean refreshStructure) {
						if (refreshStructure) {
							WindowManager.this.refreshWindowStructureAsMuchAsPossible();
						}
					}
				});
			}
			scrollPane = createScrollPane(content);
			layoutContent(scrollPane);
		}
		buttonBar = createButtonBar();
		layoutButtonBar(buttonBar);
		refreshWindowStructureAsMuchAsPossible();
		adjustBounds();
	}

	public void refreshWindowStructureAsMuchAsPossible() {
		Color backgroundColor = getMainBackgroundColor();
		Image backgroundImage = getMainBackgroundImage();
		backgroundPane.setBackground(backgroundColor);
		backgroundPane.setImage(backgroundImage);
		backgroundPane.setOpaque((backgroundColor != null) && (backgroundImage == null));
		if (alternativeDecorationsPanel != null) {
			alternativeDecorationsPanel
					.setBorder(BorderFactory.createLineBorder(getAlternativeDecorationsBorderColor(), 4));
		}
		Color borderColor = getMainBorderColor();
		if (borderColor != null) {
			buttonBar.setBorder(BorderFactory.createLineBorder(borderColor));
		} else {
			buttonBar.setBorder(BorderFactory.createRaisedBevelBorder());
		}
		SwingRendererUtils.handleComponentSizeChange(window);
	}

	protected Image getMainBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBackgroundImagePath() != null) {
			return SwingRendererUtils.loadImageThroughCache(appInfo.getMainBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		} else {
			return null;
		}
	}

	protected Color getMainBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBackgroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getMainBackgroundColor());
		} else {
			return null;
		}
	}

	protected Color getMainForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainForegroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getMainForegroundColor());
		} else {
			return null;
		}
	}

	protected Color getMainBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBorderColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getMainBorderColor());
		} else {
			return null;
		}
	}

	protected Color getTitleBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getTitleBackgroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getTitleBackgroundColor());
		} else {
			return null;
		}
	}

	protected Color getTitleForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getTitleForegroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getTitleForegroundColor());
		} else {
			return null;
		}
	}

	public void setIconImage(Image iconImage) {
		if (iconImage == null) {
			window.setIconImage(SwingRendererUtils.NULL_IMAGE);
		} else {
			window.setIconImage(iconImage);
		}
	}

	public void setTitle(String title) {
		SwingRendererUtils.setTitle(window, swingRenderer.prepareStringToDisplay(title));
	}

	@Override
	public String toString() {
		return "WindowManager [window=" + window + "]";
	}

	protected class CustomWindowDecorationsPanel extends AlternativeWindowDecorationsPanel {

		public CustomWindowDecorationsPanel(String windowTitle, Icon windowIcon, Window window,
				Component windowContent) {
			super(windowTitle, windowIcon, window, windowContent);
		}

		private static final long serialVersionUID = 1L;

		{
			getTitleLabel().setHorizontalAlignment(JLabel.LEFT);
			Font font = getTitleLabel().getFont();
			{
				font = new Font(font.getName(), Font.BOLD, font.getSize());
				getTitleLabel().setFont(font);
			}
		}

		@Override
		public Color getTitleBarColor() {
			Color result = null;
			if (result == null) {
				result = getTitleBackgroundColor();
			}
			if (result == null) {
				result = UIManager.getColor("Panel.background");
			}
			return result;
		}

		@Override
		public Color getDecorationsForegroundColor() {
			Color result = null;
			if (result == null) {
				result = getTitleForegroundColor();
			}
			if (result == null) {
				result = getMainForegroundColor();
			}
			if (result == null) {
				result = UIManager.getColor("Panel.foreground");
			}
			return result;
		}

		@Override
		protected boolean isTitleBarPainted() {
			return getTitleBackgroundColor() != null;
		}
	}

}
