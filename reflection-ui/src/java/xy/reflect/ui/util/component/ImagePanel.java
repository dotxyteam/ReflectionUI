package xy.reflect.ui.util.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	protected static final long serialVersionUID = 1L;
	protected Image image;
	protected boolean preserveRatio = false;
	protected boolean scalingQualitHigh = false;
	protected Rectangle lastScaledBounds;
	protected Image lastScaledImage;

	public ImagePanel() {
		this(null, false);
	}

	public ImagePanel(Image image, boolean preserveRatio) {
		setOpaque(false);
		if (image != null) {
			setPreferredSize(new Dimension(image.getWidth(null),
					image.getHeight(null)));
			setImage(image);
		}
		preserveRatio(preserveRatio);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		lastScaledImage = null;
		lastScaledBounds = null;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			BufferedImage bufferedImage = copy(image);
			Rectangle scaledBounds;
			if (preserveRatio) {
				Point2D.Double imageSize = new Point2D.Double(
						bufferedImage.getWidth(), bufferedImage.getHeight());
				Point2D.Double canvasSize = new Point2D.Double(getWidth(),
						getHeight());
				Rectangle.Double scaledDoubleBounds = scaleToFitInside(
						imageSize, canvasSize);
				scaledBounds = new Rectangle(
						Math.round((float) scaledDoubleBounds.x),
						Math.round((float) scaledDoubleBounds.y),
						(int) (scaledDoubleBounds.width),
						(int) (scaledDoubleBounds.height));
			} else {
				scaledBounds = new Rectangle(0, 0, getWidth(), getHeight());
			}
			Image scaledImage;
			if (scaledBounds.equals(lastScaledBounds)) {
				scaledImage = lastScaledImage;
			} else {
				int scalingQuality = scalingQualitHigh ? Image.SCALE_SMOOTH
						: Image.SCALE_FAST;
				scaledImage = bufferedImage
						.getScaledInstance(scaledBounds.width,
								scaledBounds.height, scalingQuality);
				lastScaledImage = scaledImage;
				lastScaledBounds = scaledBounds;
			}
			g.drawImage(scaledImage, scaledBounds.x, scaledBounds.y,
					scaledBounds.width, scaledBounds.height, null);
		}
	}

	public void preserveRatio(boolean b) {
		preserveRatio = b;
		repaint();
	}

	public boolean preservesRatio() {
		return preserveRatio;
	}

	public boolean isScalingQualitHigh() {
		return scalingQualitHigh;
	}

	public void setScalingQualitHigh(boolean scalingQualitHigh) {
		this.scalingQualitHigh = scalingQualitHigh;
		lastScaledImage = null;
		lastScaledBounds = null;
	}

	protected static Rectangle2D.Double scaleToFitInside(Point2D.Double size,
			Point2D.Double boxSize) {
		Point2D.Double candidateScaledSize1 = scaleToWidth(size, boxSize.x);
		Point2D.Double candidateScaledSize2 = scaleToHeight(size, boxSize.y);
		final Point2D.Double scaledSize;
		if ((candidateScaledSize1.x * candidateScaledSize1.y) < (candidateScaledSize2.x * candidateScaledSize2.y)) {
			scaledSize = candidateScaledSize1;
		} else {
			scaledSize = candidateScaledSize2;
		}
		double x = (boxSize.x - scaledSize.x) / 2.0;
		double y = (boxSize.y - scaledSize.y) / 2.0;
		return new Rectangle2D.Double(x, y, scaledSize.x, scaledSize.y);
	}

	protected static Point2D.Double scaleToHeight(Point2D.Double dimension,
			double targetHeight) {
		double targetWidth = targetHeight * dimension.x / dimension.y;
		return new Point2D.Double(targetWidth, targetHeight);
	}

	protected static Point2D.Double scaleToWidth(Point2D.Double dimension,
			double targetWidth) {
		double targetHeight = targetWidth * dimension.y / dimension.x;
		return new Point2D.Double(targetWidth, targetHeight);
	}
	
	protected static BufferedImage copy(Image image) {
		BufferedImage result = new BufferedImage(image.getWidth(null),
				image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		return result;
	}

}
