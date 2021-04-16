/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import xy.reflect.ui.util.MathUtils;

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
			configureGraphics((Graphics2D) g);
			paintImage(g, imageToPaint);
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

	protected void configureGraphics(Graphics2D g) {
		if (scalingQualitHigh) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
