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
import xy.reflect.ui.util.component.ControlPanel;
import xy.reflect.ui.util.component.ControlScrollPane;
import xy.reflect.ui.util.component.ImagePanel;
import xy.reflect.ui.util.component.ScrollPaneOptions;

public class WindowManager {

	protected Window window;
	protected SwingRenderer swingRenderer;
	protected ImagePanel contentPane;
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

	protected void layoutToolBar(JPanel toolbar) {
		contentPane.add(toolbar, BorderLayout.SOUTH);
	}

	protected JScrollPane createScrollPane(Component content) {
		return new ControlScrollPane(new ScrollPaneOptions(content, true, false));
	}

	protected void setContentPane(Container contentPane) {
		SwingRendererUtils.setContentPane(window, contentPane);
	}

	protected void layoutMenuBar(JMenuBar menuBar) {
		SwingRendererUtils.setMenuBar(window, menuBar);
	}

	protected void layoutStatusBar(Component statusBar) {
		Container contentPane = SwingRendererUtils.getContentPane(window);
		contentPane.add(statusBar, BorderLayout.NORTH);
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
		contentPane.setLayout(new BorderLayout());
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
			JScrollPane scrollPane = createScrollPane(content);
			scrollPane.getViewport().setOpaque(false);
			contentPane.add(scrollPane, BorderLayout.CENTER);
		}
		toolBar = createToolBar();
		layoutToolBar(toolBar);
		refreshWindowStructure();
		adjustBounds();
	}

	public void refreshWindowStructure() {
		if (contentPane != null) {
			ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
			IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
			Color awtBackgroundColor;
			{
				if (appInfo.getMainBackgroundColor() != null) {
					awtBackgroundColor = SwingRendererUtils.getColor(appInfo.getMainBackgroundColor());
				} else {
					awtBackgroundColor = UIManager.getColor("Panel.background");
				}
				contentPane.setBackground(awtBackgroundColor);
			}
			Image awtImage;
			{
				if (appInfo.getMainBackgroundImagePath() != null) {
					awtImage = SwingRendererUtils.loadImageThroughcache(appInfo.getMainBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				} else {
					awtImage = null;
				}
				contentPane.setImage(awtImage);
			}
			contentPane.setOpaque((awtBackgroundColor != null) && (awtImage == null));
		}
		if (toolBar != null) {
			updateToolBar();
		}
	}

	protected ImagePanel createContentPane() {
		ImagePanel result = new ImagePanel();
		result.setPreservingRatio(true);
		result.setFillingAreaWhenPreservingRatio(true);
		return result;
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
