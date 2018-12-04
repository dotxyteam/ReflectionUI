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
