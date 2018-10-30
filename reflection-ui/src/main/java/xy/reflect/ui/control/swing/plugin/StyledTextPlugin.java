package xy.reflect.ui.control.swing.plugin;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
import xy.reflect.ui.util.ReflectionUIError;

public class StyledTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "StyledText";
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

		public enum HorizontalAlignment {
			LEFT, CENTER, RIGHT
		};

		public String fontName = Font.SERIF;
		public boolean fontBold = true;
		public boolean fontItalic = true;
		public int fontSize = 20;
		public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
		public boolean underlined = false;
		public boolean struckThrough = false;

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
		public String toString() {
			return "StyledText [data=" + data + "]";
		}
	}

}
