
package xy.reflect.ui.control.swing.builder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Dialog factory.
 * 
 * @author olitank
 *
 */
public class DialogBuilder {

	protected SwingRenderer swingRenderer;

	protected String title;
	protected Image iconImage;
	protected Component ownerComponent;
	protected Component contentComponent;
	protected List<Component> buttonBarControls;
	protected RenderedDialog dialog;
	protected Image closingButtonBackgroundImage;
	protected Font closingButtonCustomFont;
	protected Color closingButtonBackgroundColor;
	protected Color closingButtonForegroundColor;
	protected Color closingButtonBorderColor;
	protected JButton standardOKButton;
	protected JButton standardCancelButton;

	public DialogBuilder(SwingRenderer swingRenderer, Component ownerComponent) {
		this.ownerComponent = ownerComponent;
		this.swingRenderer = swingRenderer;
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getIconImagePath() != null) {
			this.iconImage = SwingRendererUtils.loadImageThroughCache(appInfo.getIconImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		}
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Image getClosingButtonBackgroundImage() {
		return closingButtonBackgroundImage;
	}

	public void setClosingButtonBackgroundImage(Image buttonBackgroundImage) {
		this.closingButtonBackgroundImage = buttonBackgroundImage;
	}

	public Font getClosingButtonCustomFont() {
		return closingButtonCustomFont;
	}

	public void setClosingButtonCustomFont(Font closingButtonCustomFont) {
		this.closingButtonCustomFont = closingButtonCustomFont;
	}

	public Color getClosingButtonBackgroundColor() {
		return closingButtonBackgroundColor;
	}

	public void setClosingButtonBackgroundColor(Color buttonBackgroundColor) {
		this.closingButtonBackgroundColor = buttonBackgroundColor;
	}

	public Color getClosingButtonForegroundColor() {
		return closingButtonForegroundColor;
	}

	public void setClosingButtonForegroundColor(Color buttonForegroundColor) {
		this.closingButtonForegroundColor = buttonForegroundColor;
	}

	public Color getClosingButtonBorderColor() {
		return closingButtonBorderColor;
	}

	public void setClosingButtonBorderColor(Color buttonBorderColor) {
		this.closingButtonBorderColor = buttonBorderColor;
	}

	public RenderedDialog getCreatedDialog() {
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

	public JButton getStandardOKButton() {
		return standardOKButton;
	}

	public JButton getStandardCancelButton() {
		return standardCancelButton;
	}

	public JButton createDialogClosingButton(final String caption, final Runnable beforeClosingAction) {
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				return DialogBuilder.this.getClosingButtonBackgroundImage();
			}

			@Override
			public Font retrieveCustomFont() {
				return DialogBuilder.this.getClosingButtonCustomFont();
			}

			@Override
			public Color retrieveBackgroundColor() {
				return DialogBuilder.this.getClosingButtonBackgroundColor();
			}

			@Override
			public Color retrieveForegroundColor() {
				return DialogBuilder.this.getClosingButtonForegroundColor();
			}

			@Override
			public Color retrieveBorderColor() {
				return DialogBuilder.this.getClosingButtonBorderColor();
			}

			@Override
			public String retrieveText() {
				return swingRenderer.prepareMessageToDisplay(caption);
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
					swingRenderer.handleObjectException(result, t);
				} finally {
					dialog.dispose();
				}
			}
		});
		return result;
	}

	public List<JButton> createStandardOKCancelDialogButtons(String customOKCaption, String customCancelCaption) {
		List<JButton> result = new ArrayList<JButton>();
		result.add(standardOKButton = createDialogClosingButton((customOKCaption == null) ? "OK" : customOKCaption, new Runnable() {
			@Override
			public void run() {
				dialog.setOkPressed();
			}
		}));
		result.add(standardCancelButton = createDialogClosingButton((customCancelCaption == null) ? "Cancel" : customCancelCaption, null));
		return result;
	}

	public RenderedDialog createDialog() {
		Window owner = SwingRendererUtils.getWindowAncestorOrSelf(ownerComponent);
		dialog = new RenderedDialog(owner, this);
		dialog.setResizable(true);
		return dialog;
	}

	/**
	 * Dialog created using {@link DialogBuilder}.
	 * 
	 * @author olitank
	 *
	 */
	public static class RenderedDialog extends JDialog {

		protected static final long serialVersionUID = 1L;

		protected DialogBuilder dialogBuilder;
		protected WindowManager windowManager;
		protected boolean disposed = false;
		protected boolean okPressed = false;

		public RenderedDialog(Window owner, DialogBuilder dialogBuilder) {
			super(owner);
			this.dialogBuilder = dialogBuilder;
			this.windowManager = dialogBuilder.getSwingRenderer().createWindowManager(this);
			installComponents();
		}

		@Override
		public void dispose() {
			if (disposed) {
				return;
			}
			disposed = true;
			uninstallComponents();
			this.windowManager = null;
			this.dialogBuilder = null;
			super.dispose();
		}

		public DialogBuilder getDialogBuilder() {
			return dialogBuilder;
		}

		public boolean isDisposed() {
			return disposed;
		}

		public boolean wasOkPressed() {
			return okPressed;
		}

		public void setOkPressed() {
			this.okPressed = true;
		}

		protected void installComponents() {
			windowManager.install(dialogBuilder.getContentComponent(), dialogBuilder.getButtonBarControls(),
					dialogBuilder.getTitle(), dialogBuilder.getIconImage());
		}

		protected void uninstallComponents() {
			windowManager.uninstall();
		}

	}

}
