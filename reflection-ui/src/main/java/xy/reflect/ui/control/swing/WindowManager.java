package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
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
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ScrollPaneOptions;

public class WindowManager {

	protected Window window;
	protected SwingRenderer swingRenderer;

	public WindowManager(SwingRenderer swingRenderer, Window window) {
		this.swingRenderer = swingRenderer;
		this.window = window;
	}

	public Component createToolBar(List<? extends Component> toolbarControls) {
		JPanel result = new JPanel();
		result.setBorder(BorderFactory.createRaisedBevelBorder());
		result.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (Component tool : toolbarControls) {
			result.add(tool);
		}
		return result;
	}

	protected JScrollPane createScrollPane(Component content) {
		return new JScrollPane(new ScrollPaneOptions(content, true, false));
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

	public void set(final Component content, List<? extends Component> toolbarControls, String title, Image iconImage) {
		setTitle(title);
		setIconImage(iconImage);
		set(content, toolbarControls);
		adjustBounds();
	}

	public void adjustBounds() {
		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public void set(Component content, List<? extends Component> toolbarControls) {
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		if (content != null) {
			if (SwingRendererUtils.isForm(content, swingRenderer)) {
				final Form form = (Form) content;
				layoutMenuBar(form.getMenuBar());
				layoutStatusBar(form.getStatusBar());
				if (SwingRendererUtils.isForm(content, swingRenderer)) {
					window.addWindowListener(new WindowAdapter() {
						@Override
						public void windowOpened(WindowEvent e) {
							form.updateMenuBar();
							form.validateFormInBackgroundAndReportOnStatusBar();
							SwingRendererUtils.requestAnyComponentFocus(form, swingRenderer);
						}
					});
				}
			}
			JScrollPane scrollPane = createScrollPane(content);
			scrollPane.getViewport().setOpaque(false);
			contentPane.add(scrollPane, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			if (toolbarControls.size() > 0) {
				contentPane.add(createToolBar(toolbarControls), BorderLayout.SOUTH);
			}
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
