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
package xy.reflect.ui.control.swing.plugin;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlDimensionSpecification;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class HtmlPlugin extends StyledTextPlugin {

	@Override
	public String getControlTitle() {
		return "HTML Control";
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new HtmlConfiguration();
	}

	@Override
	public HtmlControl createControl(Object renderer, IFieldControlInput input) {
		return new HtmlControl((SwingRenderer) renderer, input);
	}

	public static class HtmlConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public IBaseURLAccessor baseURLAccessor = new FileBaseURLAccessor();
		public ControlDimensionSpecification length;
		public boolean pureHtmlColors = false;

		public URL getBaseURL() throws Exception {
			return baseURLAccessor.getURL();
		}

		public int getLenghthInPixels() {
			if (length == null) {
				return -1;
			}
			if (length.unit == ControlSizeUnit.PIXELS) {
				return length.value;
			} else if (length.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = ReflectionUIUtils.getDefaultScreenSize();
				return Math.round((length.value / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}

		public static interface IBaseURLAccessor extends Serializable {

			URL getURL() throws Exception;

		}

		public static class FileBaseURLAccessor implements IBaseURLAccessor {

			private static final long serialVersionUID = 1L;

			public File directory = new File(".");

			@Override
			public URL getURL() throws Exception {
				return directory.toURI().toURL();
			}

			public void validate() throws Exception {
				if (!directory.isDirectory()) {
					throw new ReflectionUIError("Directory not found: '" + directory.getAbsolutePath() + "'");
				}
			}

		}

		public static class ClasspathBaseURLAccessor implements IBaseURLAccessor {

			private static final long serialVersionUID = 1L;

			public String sourceClassName = "";

			@Override
			public URL getURL() throws Exception {
				Class<?> theClass = Class.forName(sourceClassName);
				String path = "/";
				Package thePackage = theClass.getPackage();
				if (thePackage != null) {
					path += thePackage.getName().replace(".", "/") + "/";
				}
				URL result = theClass.getResource(path);
				return result;
			}

			public void validate() throws Exception {
				Class.forName(sourceClassName);
			}

		}

	}

	public class HtmlControl extends StyledTextControl {

		private static final long serialVersionUID = 1L;

		public HtmlControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			JTextComponent result = super.createTextComponent();
			((JTextPane) result).addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						openWebPage(e.getURL());
					}
				}
			});
			return result;
		}

		protected void openWebPage(URL url) {
			try {
				Desktop.getDesktop().browse(url.toURI());
			} catch (Exception e) {
				throw new ReflectionUIError("Failed to display the web page '" + url + "': " + e, e);
			}
		}

		@Override
		protected void updateTextComponent(boolean refreshStructure) {
			if (refreshStructure) {
				updateTextComponentEditorKit(refreshStructure);
			}
			super.updateTextComponent(refreshStructure);
		}

		@Override
		protected void updateTextComponentStyle(boolean refreshStructure) {
			if (refreshStructure) {
				HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
				if (controlCustomization.pureHtmlColors) {
					textComponent.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
					textComponent.setOpaque(true);
					textComponent.setBackground(new JTextPane().getBackground());
				} else {
					textComponent.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
				}
			}

		}

		protected void updateTextComponentEditorKit(boolean refreshStructure) {
			listenerDisabled = true;
			try {
				HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
				((JTextPane) textComponent).setContentType("text/html");
				HTMLDocument doc = (HTMLDocument) textComponent.getDocument();
				try {
					doc.setBase(controlCustomization.getBaseURL());
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		protected void setCurrentTextEditPosition(int position) {
			return;
		}

		@Override
		protected int getConfiguredScrollPaneHeight() {
			HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
			return controlCustomization.getLenghthInPixels();
		}

		@Override
		public String toString() {
			return "HtmlControl [data=" + data + "]";
		}
	}

}
