package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

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
	protected ImagePanel contentPane;
	protected ControlPanel topBarsContainer;
	protected JScrollPane scrollPane;
	protected JPanel toolBar;
	protected Accessor<List<Component>> toolBarControlsAccessor;

	public WindowManager(SwingRenderer swingRenderer, Window window) {
		this.swingRenderer = swingRenderer;
		this.window = window;
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

	protected JScrollPane createScrollPane(Component content) {
		ControlScrollPane result = new ControlScrollPane(new ScrollPaneOptions(content, true, false));
		result.setBorder(BorderFactory.createEmptyBorder());
		return result;
	}

	protected void setContentPane(Container contentPane) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.isSystemIntegrationNative()) {
			alternativeDecorationsPanel = null;
			SwingRendererUtils.setUndecorated(window, false);
			SwingRendererUtils.setContentPane(window, contentPane);
		} else {
			alternativeDecorationsPanel = createAlternativeWindowDecorationsPanel(
					SwingRendererUtils.getWindowTitle(window), window, contentPane);
			SwingRendererUtils.setContentPane(window, alternativeDecorationsPanel);
		}
	}

	protected AlternativeWindowDecorationsPanel createAlternativeWindowDecorationsPanel(String windowTitle,
			Window window, Component windowContent) {
		return new AlternativeWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window), window, windowContent) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return getBackgroundColor();
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return getForegroundColor();
			}

		};
	}

	protected ImagePanel createContentPane() {
		ImagePanel result = new ImagePanel();
		result.setPreservingRatio(true);
		result.setFillingAreaWhenPreservingRatio(true);
		result.setLayout(new BorderLayout());
		topBarsContainer = new ControlPanel();
		{
			topBarsContainer.setLayout(new BorderLayout());
			result.add(topBarsContainer, BorderLayout.NORTH);
		}
		return result;
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
		contentPane = createContentPane();
		setContentPane(contentPane);
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
		Color backgroundColor = getBackgroundColor();
		contentPane.setBackground(backgroundColor);
		Image backgroundImage = getBackgroundImage();
		contentPane.setImage(backgroundImage);
		contentPane.setOpaque((backgroundColor != null) && (backgroundImage == null));
		updateToolBar();
		SwingRendererUtils.handleComponentSizeChange(window);
	}

	protected Image getBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBackgroundImagePath() != null) {
			return SwingRendererUtils.loadImageThroughcache(appInfo.getMainBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		} else {
			return null;
		}
	}

	protected Color getBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBackgroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getMainBackgroundColor());
		} else {
			return UIManager.getColor("Panel.background");
		}
	}

	protected Color getForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainForegroundColor() != null) {
			return SwingRendererUtils.getColor(appInfo.getMainForegroundColor());
		} else {
			return UIManager.getColor("Panel.foreground");
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
