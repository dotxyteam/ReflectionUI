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
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicBorders;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractControlButton extends JButton {

	protected static final long serialVersionUID = 1L;

	public abstract SwingRenderer getSwingRenderer();

	public abstract String retrieveCaption();

	public abstract Image retrieveBackgroundImage();

	public abstract Color retrieveBackgroundColor();

	public abstract Color retrieveForegroundColor();

	public abstract Color retrieveBorderColor();

	private boolean initialized = false;
	private SwingRenderer swingRenderer;
	private Color backgroundColor;
	private Color activatedBackgroundColor;
	private Color foregroundColor;
	private Color borderColor;
	private Image backgroundImage;
	private Image activatedBackgroundImage;
	private String caption;
	private String toolTipText;
	private Icon icon;

	public String retrieveToolTipText() {
		return null;
	}

	public Icon retrieveIcon() {
		return null;
	}

	public void updateStyle() {
		swingRenderer = getSwingRenderer();
		backgroundColor = retrieveBackgroundColor();
		foregroundColor = retrieveForegroundColor();
		borderColor = retrieveBorderColor();
		backgroundImage = retrieveBackgroundImage();
		activatedBackgroundColor = (backgroundColor == null) ? null
				: swingRenderer.addColorActivationEffect(backgroundColor);
		activatedBackgroundImage = (backgroundImage == null) ? null
				: swingRenderer.addImageActivationEffect(backgroundImage);
		caption = retrieveCaption();
		toolTipText = retrieveToolTipText();
		icon = retrieveIcon();

		setText(getSwingRenderer().prepareStringToDisplay(caption));
		if ((toolTipText != null) && (toolTipText.length() > 0)) {
			SwingRendererUtils.setMultilineToolTipText(this, getSwingRenderer().prepareStringToDisplay(toolTipText));
		} else {
			setToolTipText(null);
		}
		setIcon(icon);
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		} else {
			setBackground(new JButton().getBackground());
		}
		if (foregroundColor != null) {
			setForeground(foregroundColor);
		} else {
			setForeground(new JButton().getForeground());
		}
		if (borderColor != null) {
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor),
					new BasicBorders.MarginBorder()));
		} else {
			setBorder(new JButton().getBorder());
		}
		if ((backgroundImage != null) || (backgroundColor != null)) {
			setContentAreaFilled(false);
		} else {
			setContentAreaFilled(true);
		}
		if (backgroundImage != null) {
			setBorderPainted(false);
		} else {
			setBorderPainted(true);
		}
	}

	@Override
	public void addNotify() {
		if (!initialized) {
			updateStyle();
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

}
