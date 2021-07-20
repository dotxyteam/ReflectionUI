


package xy.reflect.ui.control.swing.plugin;

import java.io.IOException;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

/**
 * Field control plugin that allows to display only single-line text.
 * 
 * @author olitank
 *
 */
public class SingleLineTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Single Line Text";
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
		return new SingleLineTextConfiguration();
	}

	@Override
	public SingleLineTextControl createControl(Object renderer, IFieldControlInput input) {
		return new SingleLineTextControl((SwingRenderer) renderer, input);
	}

	public static class SingleLineTextConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public char invalidCharacterReplacement = ' ';

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			if (invalidCharacterReplacement == 0) {
				invalidCharacterReplacement = ' ';
			}
		}

	}

	public class SingleLineTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public SingleLineTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			JTextComponent result = super.createTextComponent();
			((AbstractDocument) result.getDocument()).setDocumentFilter(new DocumentFilter() {
				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
						throws BadLocationException {
					SingleLineTextConfiguration controlCustomization = (SingleLineTextConfiguration) loadControlCustomization(
							input);
					text = text.replace("\r\n", Character.toString(controlCustomization.invalidCharacterReplacement));
					text = text.replace("\n", Character.toString(controlCustomization.invalidCharacterReplacement));
					text = text.replace("\r", Character.toString(controlCustomization.invalidCharacterReplacement));
					super.replace(fb, offset, length, text, attrs);
				}
			});
			return result;
		}

		@Override
		protected boolean areScrollBarsEnabled() {
			return false;
		}

		@Override
		public String toString() {
			return "SingleLineTextControl [data=" + data + "]";
		}
	}

}
