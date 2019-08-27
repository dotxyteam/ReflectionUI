/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.util.component;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AutoResizeTabbedPane extends JTabbedPane {

	protected static final long serialVersionUID = 1L;
	
	public AutoResizeTabbedPane() {
		super();
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				validate();
			}

		});
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		int maxTabHeigh = 0;
		int currentTabHeight = 0;
		for (int i = 0; i < getTabCount(); i++) {
			Component tab = getComponentAt(i);
			if (tab != null) {
				Dimension tabSize = tab.getPreferredSize();
				if (tabSize != null) {
					maxTabHeigh = Math.max(maxTabHeigh, tabSize.height);
					if (i == getSelectedIndex()) {
						currentTabHeight = tabSize.height;
					}
				}
			}
		}
		result.height += (currentTabHeight - maxTabHeigh);
		return result;
	}

}
