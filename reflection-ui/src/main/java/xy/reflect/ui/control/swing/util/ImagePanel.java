


package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import xy.reflect.ui.util.MathUtils;

/**
 * Panel able to display an image.
 * 
 * @author olitank
 *
 */
public class ImagePanel extends ControlPanel {

	private static final long serialVersionUID = 1L;
	private Image image;
	private boolean preservingRatio = false;
	private boolean scalingQualitHigh = true;
	private boolean fillingAreaWhenPreservingRatio = false;
	private Color backgroundColor = null;

	public ImagePanel() {
		this(null);
	}

	public ImagePanel(Image image) {
		this(image, false);
	}

	public ImagePanel(Image image, boolean preservingRatio) {
		setBackground(null);
		setPreservingRatio(preservingRatio);
		if (image != null) {
			setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
			setImage(image);
		}
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		repaint();
	}

	public boolean isPreservingRatio() {
		return preservingRatio;
	}

	public void setPreservingRatio(boolean b) {
		preservingRatio = b;
		repaint();
	}

	public boolean isFillingAreaWhenPreservingRatio() {
		return fillingAreaWhenPreservingRatio;
	}

	public void setFillingAreaWhenPreservingRatio(boolean fillingAreaWhenPreservingRatio) {
		this.fillingAreaWhenPreservingRatio = fillingAreaWhenPreservingRatio;
		repaint();
	}

	public boolean isScalingQualitHigh() {
		return scalingQualitHigh;
	}

	public void setScalingQualitHigh(boolean scalingQualitHigh) {
		this.scalingQualitHigh = scalingQualitHigh;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Image imageToPaint = image;
		if (imageToPaint != null) {
			Runnable paintAction = new Runnable() {
				@Override
				public void run() {
					paintImage(g, imageToPaint);
				}
			};
			if (scalingQualitHigh && (g instanceof Graphics2D)) {
				SwingRendererUtils.withHighQualityScaling((Graphics2D) g, paintAction);
			} else {
				paintAction.run();
			}
		}
	}

	protected Rectangle getImageSourceBounds(Image imageToPaint) {
		return new Rectangle(0, 0, imageToPaint.getWidth(null), imageToPaint.getHeight(null));
	}

	protected Rectangle getImageDestinationBounds(Image imageToPaint) {
		if (preservingRatio) {
			Point2D.Double imageSize = MathUtils
					.toDoublePoint(new Dimension(imageToPaint.getWidth(null), imageToPaint.getHeight(null)));
			Point2D.Double canvasSize = MathUtils.toDoublePoint(new Dimension(getWidth(), getHeight()));
			if (fillingAreaWhenPreservingRatio) {
				return MathUtils.toInteger(MathUtils.scaleToBound(imageSize, canvasSize));
			} else {
				return MathUtils.toInteger(MathUtils.scaleToFitInside(imageSize, canvasSize));
			}
		} else {
			return new Rectangle(0, 0, getWidth(), getHeight());
		}
	}

	protected void paintImage(Graphics g, Image imageToPaint) {
		Rectangle srcBounds = getImageSourceBounds(imageToPaint);
		Rectangle dstBounds = getImageDestinationBounds(imageToPaint);
		int sx1 = srcBounds.x;
		int sy1 = srcBounds.y;
		int sx2 = srcBounds.x + srcBounds.width;
		int sy2 = srcBounds.y + srcBounds.height;
		int dx1 = dstBounds.x;
		int dy1 = dstBounds.y;
		int dx2 = dstBounds.x + dstBounds.width;
		int dy2 = dstBounds.y + dstBounds.height;
		g.drawImage(imageToPaint, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, backgroundColor, null);
	}

}
