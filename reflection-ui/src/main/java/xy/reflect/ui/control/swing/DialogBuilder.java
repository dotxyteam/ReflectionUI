package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class DialogBuilder {

	protected SwingRenderer swingRenderer;

	protected String title;
	protected Image iconImage;
	protected Component ownerComponent;
	protected Component contentComponent;
	protected List<? extends Component> toolbarComponents;
	protected Runnable whenClosing;
	protected boolean okPressed = false;

	protected JDialog dialog;

	public DialogBuilder(SwingRenderer swingRenderer, Component ownerComponent) {
		super();
		this.ownerComponent = ownerComponent;
		this.swingRenderer = swingRenderer;
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

	public List<? extends Component> getToolbarComponents() {
		return toolbarComponents;
	}

	public void setToolbarComponents(List<? extends Component> toolbarComponents) {
		this.toolbarComponents = toolbarComponents;
	}

	public Runnable getWhenClosing() {
		return whenClosing;
	}

	public void setWhenClosing(Runnable whenClosing) {
		this.whenClosing = whenClosing;
	}

	public JButton createDialogClosingButton(String caption, final Runnable beforeClosingAction) {
		final JButton result = new JButton(swingRenderer.prepareStringToDisplay(caption));
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
				if (swingRenderer.getDataUpdateDelayMilliseconds() > 0) {
					try {
						Thread.sleep(swingRenderer.getDataUpdateDelayMilliseconds());
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
				}
				super.dispose();
				if (whenClosing != null) {
					try {
						whenClosing.run();
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(this, t);
					}
				}
				disposed = true;
			}
		};
		WindowManager windowManager = swingRenderer.createWindowManager(dialog);
		windowManager.set(contentComponent, toolbarComponents, title, iconImage);
		dialog.setResizable(true);
		return dialog;
	}
}
