package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class StyledTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Styled Text";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return String.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new StyledTextConfiguration();
	}

	@Override
	public StyledTextControl createControl(Object renderer, IFieldControlInput input) {
		return new StyledTextControl((SwingRenderer) renderer, input);
	}

	public static class StyledTextConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String fontName = Font.SERIF;
		public boolean fontBold = true;
		public boolean fontItalic = true;
		public int fontSize = 20;
		public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		public boolean underlined = false;
		public boolean struckThrough = false;
		public ControlDimensionSpecification length;

		public String[] getFontNames() {
			List<String> result = new ArrayList<String>();
			result.addAll(
					Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
			return result.toArray(new String[result.size()]);
		}

		public int getFontStyle() {
			int result = 0;
			if (fontBold) {
				result |= Font.BOLD;
			}
			if (fontItalic) {
				result |= Font.ITALIC;
			}
			return result;
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

		public enum HorizontalAlignment {
			LEFT, CENTER, RIGHT
		};

		public enum ControlSizeUnit {
			PIXELS, SCREEN_PERCENT
		}

		public static class ControlDimensionSpecification implements Serializable {

			private static final long serialVersionUID = 1L;

			public int value = 40;
			public ControlSizeUnit unit = ControlSizeUnit.SCREEN_PERCENT;

		}
	}

	public class StyledTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public StyledTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			return new JTextPane() {

				private static final long serialVersionUID = 1L;

				@Override
				public void replaceSelection(String content) {
					boolean listenerWasDisabled = listenerDisabled;
					listenerDisabled = true;
					try {
						super.replaceSelection(content);
					} finally {
						listenerDisabled = listenerWasDisabled;
					}
					textComponentEditHappened();
				}

				@Override
				public void setText(String t) {
					super.setText(t);
					updateScrollPolicy();
				}

			};
		}

		@Override
		protected void updateTextComponent(boolean refreshStructure) {
			super.updateTextComponent(refreshStructure);
			updateTextComponentStyle(refreshStructure);

		}

		protected void updateTextComponentStyle(boolean refreshStructure) {
			StyledTextConfiguration controlCustomization = (StyledTextConfiguration) loadControlCustomization(input);
			StyledDocument document = (StyledDocument) textComponent.getDocument();
			SimpleAttributeSet attributes = new SimpleAttributeSet();
			StyleConstants.setBold(attributes, controlCustomization.fontBold);
			StyleConstants.setItalic(attributes, controlCustomization.fontItalic);
			StyleConstants.setFontFamily(attributes, controlCustomization.fontName);
			StyleConstants.setFontSize(attributes, controlCustomization.fontSize);
			StyleConstants.setUnderline(attributes, controlCustomization.underlined);
			StyleConstants.setStrikeThrough(attributes, controlCustomization.struckThrough);
			if (controlCustomization.horizontalAlignment == StyledTextConfiguration.HorizontalAlignment.LEFT) {
				StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_LEFT);
			} else if (controlCustomization.horizontalAlignment == StyledTextConfiguration.HorizontalAlignment.CENTER) {
				StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER);
			} else if (controlCustomization.horizontalAlignment == StyledTextConfiguration.HorizontalAlignment.RIGHT) {
				StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_RIGHT);
			} else {
				throw new ReflectionUIError();
			}
			listenerDisabled = true;
			try {
				document.setParagraphAttributes(0, document.getLength(), attributes, false);
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		protected Dimension getScrollPaneSize(JScrollPane scrollPane, Dimension defaultSize) {
			int configuredHeight = getConfiguredScrollPaneHeight();
			if (configuredHeight == -1) {
				return super.getScrollPaneSize(scrollPane, defaultSize);
			}
			Dimension result = super.getScrollPaneSize(scrollPane, defaultSize);
			result.height = configuredHeight;
			return result;
		}

		protected int getConfiguredScrollPaneHeight() {
			StyledTextConfiguration controlCustomization = (StyledTextConfiguration) loadControlCustomization(input);
			return controlCustomization.getLenghthInPixels();
		}

		@Override
		public String toString() {
			return "StyledText [data=" + data + "]";
		}
	}

}
