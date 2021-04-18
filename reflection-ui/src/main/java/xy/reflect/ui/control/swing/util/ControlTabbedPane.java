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
package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.TabbedPaneUI;

public class ControlTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	public ControlTabbedPane() {
		setOpaque(false);
	}

	@Override
	public void setUI(TabbedPaneUI newUI) {
		if (newUI instanceof javax.swing.plaf.basic.BasicTabbedPaneUI) {
			newUI = new javax.swing.plaf.basic.BasicTabbedPaneUI() {

				protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
					if (!isOpaque()) {
						int width = tabPane.getWidth();
						int height = tabPane.getHeight();
						Insets insets = tabPane.getInsets();
						Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

						int x = insets.left;
						int y = insets.top;
						int w = width - insets.right - insets.left;
						int h = height - insets.top - insets.bottom;

						boolean tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
						switch (tabPlacement) {
						case LEFT:
							x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
							if (tabsOverlapBorder) {
								x -= tabAreaInsets.right;
							}
							w -= (x - insets.left);
							break;
						case RIGHT:
							w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
							if (tabsOverlapBorder) {
								w += tabAreaInsets.left;
							}
							break;
						case BOTTOM:
							h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
							if (tabsOverlapBorder) {
								h += tabAreaInsets.top;
							}
							break;
						case TOP:
						default:
							y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
							if (tabsOverlapBorder) {
								y -= tabAreaInsets.bottom;
							}
							h -= (y - insets.top);
						}

						paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);

					} else {
						super.paintContentBorder(g, tabPlacement, selectedIndex);
					}
				}

				@Override
				protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w,
						int h, boolean isSelected) {
					if (isOpaque()) {
						super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
					}
				}

				@Override
				protected void installDefaults() {
					super.installDefaults();
					if (!isOpaque()) {
						lightHighlight = getLightHighlightColor();
						shadow = getShadowColor();
						darkShadow = getDarkShadowColor();
					}
				}

			};
		}
		super.setUI(newUI);
	}

	protected Color getTabBorderColor() {
		return null;
	}

	protected Color getDarkShadowColor() {
		if (getTabBorderColor() != null) {
			return getTabBorderColor();
		}
		return UIManager.getColor("TabbedPane.shadow");
	}

	protected Color getShadowColor() {
		if (getTabBorderColor() != null) {
			return getTabBorderColor();
		}
		return UIManager.getColor("TabbedPane.shadow");
	}

	protected Color getLightHighlightColor() {
		if (getTabBorderColor() != null) {
			return getTabBorderColor();
		}
		return UIManager.getColor("TabbedPane.shadow");
	}

}
