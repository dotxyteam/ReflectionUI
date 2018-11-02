package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlDimensionSpecification;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin.StyledTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class HtmlPlugin extends StyledTextPlugin {

	@Override
	public String getControlTitle() {
		return "HTML";
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
				Dimension screenSize = SwingRendererUtils.getDefaultScreenSize();
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
				return Class.forName(sourceClassName).getResource(".");
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
					textComponent.updateUI();
				} else {
					textComponent.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
					textComponent.updateUI();
				}

			}

		}

		protected void updateTextComponentEditorKit(boolean refreshStructure) {
			listenerDisabled = true;
			try {
				HtmlConfiguration controlCustomization = (HtmlConfiguration) loadControlCustomization(input);
				if (data.isGetOnly()) {
					((JTextPane) textComponent).setContentType("text/html");
					HTMLDocument doc = (HTMLDocument) textComponent.getDocument();
					try {
						doc.setBase(controlCustomization.getBaseURL());
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				} else {
					((JTextPane) textComponent).setContentType("text/plain");
				}
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		protected void setCurrentTextEditPosition(int position) {
			if (data.isGetOnly()) {
				return;
			}
			super.setCurrentTextEditPosition(position);
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
