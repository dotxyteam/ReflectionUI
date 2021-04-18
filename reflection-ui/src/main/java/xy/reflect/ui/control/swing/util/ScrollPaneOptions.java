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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollPaneOptions extends ControlPanel implements Scrollable {

	protected static final long serialVersionUID = 1L;
	protected boolean limitWidthToViewport;
	protected boolean limitHeightToViewport;

	public ScrollPaneOptions(Component content, boolean limitWidthToViewport,
			boolean limitHeightToViewport) {
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		this.limitWidthToViewport = limitWidthToViewport;
		this.limitHeightToViewport = limitHeightToViewport;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height
				: visibleRect.width) - 10;
	}

	public boolean getScrollableTracksViewportWidth() {
		return limitWidthToViewport;
	}

	public boolean getScrollableTracksViewportHeight() {
		return limitHeightToViewport;
	}

}
