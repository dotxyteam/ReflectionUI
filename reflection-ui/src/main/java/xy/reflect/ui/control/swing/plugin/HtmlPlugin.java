
package xy.reflect.ui.control.swing.plugin;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlDimensionSpecification;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.util.AlternativeDesktopApi;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control plugin that allows to display/update text formatted using HTML.
 * 
 * @author olitank
 *
 */
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
		public ControlDimensionSpecification width = new ControlDimensionSpecification();
		public ControlDimensionSpecification height;
		public boolean pureHtmlColors = false;

		public URL getBaseURL() throws Exception {
			return baseURLAccessor.getURL();
		}

		public int getWidthInPixels() {
			if (width == null) {
				return -1;
			}
			if (width.unit == ControlSizeUnit.PIXELS) {
				return width.value;
			} else if (width.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((width.value / 100f) * screenSize.width);
			} else {
				throw new ReflectionUIError();
			}
		}

		public int getHeightInPixels() {
			if (height == null) {
				return -1;
			}
			if (height.unit == ControlSizeUnit.PIXELS) {
				return height.value;
			} else if (height.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((height.value / 100f) * screenSize.height);
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
				/*
				 * theClass.getResource(".") returns an URL that may point to a wrong existing
				 * path to a folder in the wrong jar or disk directory. To solve this issue, we
				 * need to force this URL to be the parent of the theClass URL.
				 */
				URL theClassURL = theClass.getResource(theClass.getSimpleName() + ".class");
				String theClassURLSpecification = theClassURL.toString();
				String theClassParentUrlSpecification = theClassURLSpecification.substring(0,
						theClassURLSpecification.length() - (theClass.getSimpleName() + ".class").length());
				URL result = new URL(theClassParentUrlSpecification);
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
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			super.refreshUI(refreshStructure);
			if (refreshStructure) {
				if (data.getCaption().length() > 0) {
					setBorder(
							BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
					if (data.getLabelForegroundColor() != null) {
						((TitledBorder) getBorder())
								.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					}
					if (data.getBorderColor() != null) {
						((TitledBorder) getBorder()).setBorder(
								BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					}
				} else {
					setBorder(BorderFactory.createEmptyBorder());
				}
				textComponent.setBorder(BorderFactory.createEmptyBorder());
			}
			return true;
		}

		@Override
		protected JTextComponent createTextComponent() {
			JTextComponent result = super.createTextComponent();
			((JTextPane) result).addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try {
							openWebPage(e.getURL());
						} catch (Throwable t) {
							swingRenderer.handleException(HtmlControl.this, t);
						}

					}
				}
			});
			return result;
		}

		protected void openWebPage(URL url) {
			URI uri;
			try {
				uri = url.toURI();
			} catch (URISyntaxException e) {
				throw new ReflectionUIError(e);
			}
			try {
				Desktop.getDesktop().browse(uri);
			} catch (Exception e) {
				if (!new AlternativeDesktopApi() {

					@Override
					protected void logErr(String msg, Throwable t) {
						swingRenderer.getReflectionUI().logDebug(new ReflectionUIError(msg, t));
					}

					@Override
					protected void logErr(String msg) {
						swingRenderer.getReflectionUI().logDebug(msg);
					}

					@Override
					protected void logOut(String msg) {
						swingRenderer.getReflectionUI().logDebug(msg);
					}

				}.browse(uri)) {
					throw new ReflectionUIError("Failed to display the web page '" + url + "': " + e, e);
				}
			}
		}

		@Override
		protected void refreshTextComponent(boolean refreshStructure) {
			if (refreshStructure) {
				updateTextComponentEditorKit(refreshStructure);
			}
			super.refreshTextComponent(refreshStructure);
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
		protected void restoringCaretPosition(Runnable runnable) {
			runnable.run();
		}

		@Override
		protected int getConfiguredScrollPaneWidth() {
			HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
			return controlCustomization.getWidthInPixels();
		}

		@Override
		protected int getConfiguredScrollPaneHeight() {
			HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
			return controlCustomization.getHeightInPixels();
		}

	}

}
