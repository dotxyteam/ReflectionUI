package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.JButton;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControl extends JButton implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;
	protected Color backgroundColor;
	protected Color foregroundColor;
	protected Image backgroundImage;
	protected Image activatedBackgroundImage;

	public MethodControl(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		if (data.getBackgroundImagePath() == null) {
			this.backgroundImage = null;
		} else {
			this.backgroundImage = SwingRendererUtils.loadImageThroughcache(data.getBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
			if (this.backgroundImage != null) {
				this.activatedBackgroundImage = addBackgroundImageActivationEffect(this.backgroundImage);
			}
		}
		if (data.getBackgroundColor() == null) {
			this.backgroundColor = null;
		} else {
			this.backgroundColor = SwingRendererUtils.getColor(data.getBackgroundColor());
		}
		if (data.getForegroundColor() == null) {
			this.foregroundColor = null;
		} else {
			this.foregroundColor = SwingRendererUtils.getColor(data.getForegroundColor());
		}
		initialize();
	}

	protected void initialize() {
		String caption = ReflectionUIUtils.formatMethodControlCaption(data);
		setText(swingRenderer.prepareStringToDisplay(caption));
		String toolTipText = ReflectionUIUtils.formatMethodControlTooltipText(data);
		if (toolTipText.length() > 0) {
			SwingRendererUtils.setMultilineToolTipText(this, swingRenderer.prepareStringToDisplay(toolTipText));
		}
		setIcon(SwingRendererUtils.getMethodIcon(swingRenderer, data));
		addActionListener(this);
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		}
		if (backgroundImage != null) {
			setContentAreaFilled(false);
			setBorderPainted(false);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (backgroundImage != null) {
			if (getModel().isArmed()) {
				g.drawImage(activatedBackgroundImage, 0, 0, getWidth(), getHeight(), null);
			} else {
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
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

	@Override
	public void actionPerformed(ActionEvent e) {
		MethodAction action = swingRenderer.createMethodAction(input);
		try {
			action.actionPerformed(e);
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(MethodControl.this, t);
		}
	}

	@Override
	public String toString() {
		return "MethodControl [data=" + data + "]";
	}

}
