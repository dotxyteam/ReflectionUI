package xy.reflect.ui.control.swing.renderer;

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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AlternativeWindowDecorationsPanel;
import xy.reflect.ui.util.component.ControlPanel;
import xy.reflect.ui.util.component.ControlScrollPane;
import xy.reflect.ui.util.component.ImagePanel;
import xy.reflect.ui.util.component.ScrollPaneOptions;

public class WindowManager {

	protected SwingRenderer swingRenderer;
	protected Window window;
	protected AlternativeWindowDecorationsPanel alternativeDecorationsPanel;
	protected ControlPanel rootPane;
	protected ImagePanel backgroundPane;
	protected ControlPanel contentPane;
	protected ControlPanel topBarsContainer;
	protected JScrollPane scrollPane;
	protected JPanel toolBar;
	protected Accessor<List<Component>> toolBarControlsAccessor;

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
		return new AlternativeWindowDecorationsPanel(title, icon, window, windowContent) {

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

		};
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

	protected JPanel createToolBar() {
		JPanel result = new ControlPanel();
		result.setBorder(BorderFactory.createRaisedBevelBorder());
		result.setLayout(new FlowLayout(FlowLayout.CENTER));
		return result;
	}

	protected void updateToolBar() {
		if (toolBar == null) {
			return;
		}
		List<? extends Component> toolBarControls;
		if (toolBarControlsAccessor == null) {
			toolBarControls = null;
		} else {
			toolBarControls = toolBarControlsAccessor.get();
		}
		toolBar.removeAll();
		if ((toolBarControls != null) && (toolBarControls.size() > 0)) {
			toolBar.setVisible(true);
			for (Component tool : toolBarControls) {
				toolBar.add(tool);
			}
		} else {
			toolBar.setVisible(false);
		}
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

	protected void layoutToolBar(JPanel toolbar) {
		contentPane.add(toolbar, BorderLayout.SOUTH);
	}

	public void set(final Component content, Accessor<List<Component>> toolbarControlsAccessor, String title,
			Image iconImage) {
		setTitle(title);
		setIconImage(iconImage);
		set(content, toolbarControlsAccessor);
	}

	public void adjustBounds() {
		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public void set(Component content, Accessor<List<Component>> toolbarControlsAccessor) {
		this.toolBarControlsAccessor = toolbarControlsAccessor;
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
							WindowManager.this.refreshWindowStructure();
						}
					}
				});
			}
			scrollPane = createScrollPane(content);
			layoutContent(scrollPane);
		}
		toolBar = createToolBar();
		layoutToolBar(toolBar);
		refreshWindowStructure();
		adjustBounds();
	}

	public void refreshWindowStructure() {
		Color backgroundColor = getMainBackgroundColor();
		Image backgroundImage = getMainBackgroundImage();
		backgroundPane.setBackground(backgroundColor);
		backgroundPane.setImage(backgroundImage);
		backgroundPane.setOpaque((backgroundColor != null) && (backgroundImage == null));
		if (alternativeDecorationsPanel != null) {
			alternativeDecorationsPanel
					.setBorder(BorderFactory.createLineBorder(getAlternativeDecorationsBorderColor(), 4));
		}
		updateToolBar();
		SwingRendererUtils.handleComponentSizeChange(window);
	}

	protected Image getMainBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBackgroundImagePath() != null) {
			return SwingRendererUtils.loadImageThroughcache(appInfo.getMainBackgroundImagePath(),
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

}
