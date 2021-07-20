


package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control plugin that allows to display/update styled text.
 * 
 * @author olitank
 *
 */
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
		public ColorSpecification color;

		public BufferedImage getSampleTextImage() {
			String text = fontName;
			int style = getFontStyle();
			Font font = new Font(fontName, style, fontSize);
			Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
			g2d.setFont(font);
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int spacing = fontMetrics.stringWidth(" ");
			int spacingAboveLine = spacing;
			int spacingUnderLine = spacing;
			int width = fontMetrics.stringWidth(text) + (spacing * 2);
			int height = fontMetrics.getHeight() + spacingAboveLine + spacingUnderLine;
			g2d.dispose();
			BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			g2d = result.createGraphics();
			g2d.setFont(font);
			if (color != null) {
				g2d.setColor(SwingRendererUtils.getColor(color));
			} else {
				g2d.setColor(Color.BLACK);
			}
			fontMetrics = g2d.getFontMetrics();
			g2d.drawString(text, spacing, spacingAboveLine + fontMetrics.getAscent());
			g2d.dispose();
			return result;
		}

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
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
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
		
		protected StyledTextConfiguration controlCustomization;

		public StyledTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		protected StyledTextConfiguration getOrLoadControlCustomization() {
			if (controlCustomization == null) {
				controlCustomization = (StyledTextConfiguration) loadControlCustomization(input);
			}
			return controlCustomization;
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if(refreshStructure) {
				controlCustomization = null;
			}
			return super.refreshUI(refreshStructure);
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
			StyledTextConfiguration controlCustomization = getOrLoadControlCustomization();
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
			if (controlCustomization.color != null) {
				textComponent.setForeground(SwingRendererUtils.getColor(controlCustomization.color));
			}
			listenerDisabled = true;
			try {
				document.setParagraphAttributes(0, document.getLength(), attributes, false);
			} finally {
				listenerDisabled = false;
			}
		}

		@Override
		protected Dimension getDynamicPreferredSize(ControlScrollPane scrollPane, Dimension defaultSize) {
			Dimension result = super.getDynamicPreferredSize(scrollPane, defaultSize);
			int configuredHeight = getConfiguredScrollPaneHeight();
			if (configuredHeight != -1) {
				result.height = configuredHeight;
			}
			return result;
		}

		protected int getConfiguredScrollPaneHeight() {
			return getOrLoadControlCustomization().getLenghthInPixels();
		}

		@Override
		public String toString() {
			return "StyledText [data=" + data + "]";
		}
	}

}
