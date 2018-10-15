package xy.reflect.ui.util.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.Icon;
import javax.swing.JButton;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractControlButton extends JButton {

	protected static final long serialVersionUID = 1L;

	public abstract SwingRenderer getSwingRenderer();

	public abstract String retrieveCaption();

	private boolean initialized = false;
	private SwingRenderer swingRenderer;
	private IApplicationInfo applicationInfo;
	private Color backgroundColor;
	private Color activatedBackgroundColor;
	private Color foregroundColor;
	private Image backgroundImage;
	private Image activatedBackgroundImage;
	private String caption;
	private String toolTipText;
	private Icon icon;

	protected boolean isApplicationInfoStyleLoaded() {
		return true;
	}

	protected boolean isApplicationStyleButtonSpecific() {
		return true;
	}

	public Image retrieveBackgroundImage() {
		if (!isApplicationInfoStyleLoaded()) {
			return null;
		}
		if (isApplicationStyleButtonSpecific()) {
			if (applicationInfo.getButtonBackgroundImagePath() == null) {
				return null;
			} else {
				return SwingRendererUtils.loadImageThroughcache(applicationInfo.getButtonBackgroundImagePath(),
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
			}
		} else {
			return null;
		}
	}

	public Color retrieveBackgroundColor() {
		if (!isApplicationInfoStyleLoaded()) {
			return null;
		}
		if (isApplicationStyleButtonSpecific()) {
			if (applicationInfo.getButtonBackgroundColor() == null) {
				return null;
			} else {
				return SwingRendererUtils.getColor(applicationInfo.getButtonBackgroundColor());
			}
		} else {
			if (applicationInfo.getMainBackgroundColor() == null) {
				return null;
			} else {
				return SwingRendererUtils.getColor(applicationInfo.getMainBackgroundColor());
			}
		}
	}

	public Color retrieveForegroundColor() {
		if (!isApplicationInfoStyleLoaded()) {
			return null;
		}
		if (isApplicationStyleButtonSpecific()) {
			if (applicationInfo.getButtonForegroundColor() == null) {
				return null;
			} else {
				return SwingRendererUtils.getColor(applicationInfo.getButtonForegroundColor());
			}
		} else {
			if (applicationInfo.getMainForegroundColor() == null) {
				return null;
			} else {
				return SwingRendererUtils.getColor(applicationInfo.getMainForegroundColor());
			}
		}

	}

	public String retrieveToolTipText() {
		return null;
	}

	public Icon retrieveIcon() {
		return null;
	}

	protected void initialize() {
		swingRenderer = getSwingRenderer();
		applicationInfo = swingRenderer.getReflectionUI().getApplicationInfo();
		backgroundColor = retrieveBackgroundColor();
		foregroundColor = retrieveForegroundColor();
		backgroundImage = retrieveBackgroundImage();
		activatedBackgroundColor = (backgroundColor == null) ? null
				: addBackgroundColorActivationEffect(backgroundColor);
		activatedBackgroundImage = (backgroundImage == null) ? null
				: addBackgroundImageActivationEffect(backgroundImage);
		caption = retrieveCaption();
		toolTipText = retrieveToolTipText();
		icon = retrieveIcon();

		setText(getSwingRenderer().prepareStringToDisplay(caption));
		if ((toolTipText != null) && (toolTipText.length() > 0)) {
			SwingRendererUtils.setMultilineToolTipText(this, getSwingRenderer().prepareStringToDisplay(toolTipText));
		}
		setIcon(icon);
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		}
		if ((backgroundImage != null) || (backgroundColor != null)) {
			setContentAreaFilled(false);
		}
		if (isApplicationInfoStyleLoaded()) {
			if (!isApplicationStyleButtonSpecific()) {
				if (applicationInfo.getMainBackgroundImagePath() != null) {
					setContentAreaFilled(false);
				}
			}
		}
		if (backgroundImage != null) {
			setBorderPainted(false);
		}
	}

	@Override
	public void addNotify() {
		if (!initialized) {
			initialize();
			initialized = true;
		}
		super.addNotify();
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (backgroundImage != null) {
			if (getModel().isArmed()) {
				g.drawImage(activatedBackgroundImage, 0, 0, getWidth(), getHeight(), null);
			} else {
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
			}
		} else if (backgroundColor != null) {
			if (getModel().isArmed()) {
				g.setColor(activatedBackgroundColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			} else if (getModel().isEnabled()) {
				g.setColor(backgroundColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
		super.paintComponent(g);
	}

	protected BufferedImage addBackgroundImageActivationEffect(Image backgroundImage) {
		BufferedImage result = new BufferedImage(backgroundImage.getWidth(null), backgroundImage.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = result.createGraphics();
		g.drawImage(backgroundImage, 0, 0, null);
		g.dispose();
		float scalefactor = 0.5f;
		float offset = 64f;
		return new RescaleOp(new float[] { scalefactor, scalefactor, scalefactor, 1f },
				new float[] { offset, offset, offset, 0f }, null).filter(result, null);
	}

	protected Color addBackgroundColorActivationEffect(Color color) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		if (hsb[2] > 0.5f) {
			hsb[2] -= 0.2f;
		} else {
			hsb[2] += 0.2f;
		}
		int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		return new Color(rgb);
	}

}
