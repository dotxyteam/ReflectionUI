
package xy.reflect.ui.control.swing.util;

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
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.Form.IRefreshListener;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Helper class allowing to configure windows adequately.
 * 
 * @author olitank
 *
 */
public class WindowManager {

	protected static final Color DEFAULT_TITLE_BAR_FOREROUND_COLOR = Color.BLACK;

	protected SwingRenderer swingRenderer;
	protected Window window;
	protected AlternativeWindowDecorationsPanel alternativeDecorationsPanel;
	protected JPanel rootPane;
	protected ImagePanel backgroundPane;
	protected JPanel contentPane;
	protected JPanel topBarsContainer;
	protected JScrollPane scrollPane;
	protected JPanel buttonBar;
	protected Form form;
	protected WindowListener windowListener = new WindowAdapter() {
		@Override
		public void windowOpened(WindowEvent e) {
			if (form == null) {
				return;
			}
			form.validateFormInBackgroundAndReportOnStatusBar();
			SwingRendererUtils.requestAnyComponentFocus(form, swingRenderer);
		}
	};
	protected IRefreshListener formRefreshListener = new Form.IRefreshListener() {
		@Override
		public void onRefresh(boolean refreshStructure) {
			if (refreshStructure) {
				WindowManager.this.refreshWindowStructureAsMuchAsPossible();
			}
		}
	};

	public WindowManager(SwingRenderer swingRenderer, Window window) {
		this.swingRenderer = swingRenderer;
		this.window = window;
	}

	protected JPanel createRootPane() {
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
		Color mainBackgroundColor = getMainBackgroundColor();
		if (mainBackgroundColor == null) {
			mainBackgroundColor = new JPanel().getBackground();
		}
		Color titleBackgroundColor = getAlternativeDecorationsTitleBarBackgroundColor();
		if (titleBackgroundColor == null) {
			titleBackgroundColor = new JPanel().getBackground();
		}
		Color averageBackgroundColor = new Color((mainBackgroundColor.getRed() + titleBackgroundColor.getRed()) / 2,
				(mainBackgroundColor.getGreen() + titleBackgroundColor.getGreen()) / 2,
				(mainBackgroundColor.getBlue() + titleBackgroundColor.getBlue()) / 2);
		return averageBackgroundColor;
	}

	protected JScrollPane createScrollPane(Component content) {
		ControlScrollPane result = new ControlScrollPane(new ScrollPaneOptions(content, true, false));
		result.setBorder(BorderFactory.createEmptyBorder());
		return result;
	}

	protected JPanel createContentPane() {
		ControlPanel result = new ControlPanel();
		result.setLayout(new BorderLayout());
		topBarsContainer = new ControlPanel();
		{
			topBarsContainer.setLayout(new BorderLayout());
			result.add(topBarsContainer, BorderLayout.NORTH);
		}
		return result;
	}

	protected JPanel createButtonBar(List<Component> buttonBarControls) {
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

	protected void layoutRootPane(JPanel rootPane) {
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

	public void adjustBounds() {
		SwingRendererUtils.adjustWindowInitialBounds(window);
		SwingRendererUtils.handleComponentSizeChange(window);
	}

	public void install(final Component content, List<Component> buttonBarControls, String title, Image iconImage) {
		setTitle(title);
		setIconImage(iconImage);
		install(content, buttonBarControls);
	}

	public void install(Component content, List<Component> buttonBarControls) {
		rootPane = createRootPane();
		layoutRootPane(rootPane);
		backgroundPane = createBackgroundPane();
		layoutBackgroundPane(backgroundPane);
		contentPane = createContentPane();
		layoutContentPane(contentPane);
		form = null;
		if (content != null) {
			if (SwingRendererUtils.isForm(content, swingRenderer)) {
				form = (Form) content;
				form.updateMenuBar();
				layoutMenuBar(form.getMenuBar());
				layoutStatusBar(form.getStatusBar());
				form.getRefreshListeners().add(formRefreshListener);
			}
			scrollPane = createScrollPane(content);
			layoutContent(scrollPane);
		}
		buttonBar = createButtonBar(buttonBarControls);
		layoutButtonBar(buttonBar);
		refreshWindowStructureAsMuchAsPossible();
		adjustBounds();
		window.addWindowListener(windowListener);
		if (window instanceof JFrame) {
			((JFrame) window).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		} else if (window instanceof JDialog) {
			((JDialog) window).setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		} else {
			throw new ReflectionUIError();
		}
	}

	public void uninstall() {
		window.removeWindowListener(windowListener);
		SwingRendererUtils.setContentPane(window, new ControlPanel());
		{
			alternativeDecorationsPanel = null;
			scrollPane = null;
			rootPane = null;
			backgroundPane = null;
			contentPane = null;
			topBarsContainer = null;
			buttonBar = null;
		}
		if (form != null) {
			form.getRefreshListeners().remove(formRefreshListener);
			form = null;
		}
	}

	public void refreshWindowStructureAsMuchAsPossible() {
		Color backgroundColor = getMainBackgroundColor();
		Image backgroundImage = getMainBackgroundImage();
		backgroundPane.setBackground(backgroundColor);
		backgroundPane.setImage(backgroundImage);
		backgroundPane.setOpaque((backgroundColor != null) && (backgroundImage == null));
		if (alternativeDecorationsPanel != null) {
			alternativeDecorationsPanel.repaint();
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
			return new JPanel().getBackground();
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

	protected Color getAlternativeDecorationsTitleBarBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getTitleBackgroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getTitleBackgroundColor());
		} else {
			return null;
		}
	}

	protected Color getAlternativeDecorationsTitleBarForegroundColor() {
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
		SwingRendererUtils.setTitle(window, swingRenderer.prepareMessageToDisplay(title));
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
		protected boolean isTitleBarBackgroundPainted() {
			return WindowManager.this.getAlternativeDecorationsTitleBarBackgroundColor() != null;
		}

		@Override
		public Color getTitleBarBackgroundColor() {
			return WindowManager.this.getAlternativeDecorationsTitleBarBackgroundColor();
		}

		@Override
		public Color getTitleBarForegroundColor() {
			Color result = null;
			if (result == null) {
				result = WindowManager.this.getAlternativeDecorationsTitleBarForegroundColor();
			}
			if (result == null) {
				result = DEFAULT_TITLE_BAR_FOREROUND_COLOR;
			}
			return result;
		}

		@Override
		protected boolean isBorderPainted() {
			return true;
		}

		@Override
		protected Color getBorderColor() {
			return WindowManager.this.getAlternativeDecorationsBorderColor();
		}

	}

}
