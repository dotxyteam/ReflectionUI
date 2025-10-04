
package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.plugin.MultipleLinesTextPlugin.MultipleLinesTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
		public ControlDimensionSpecification width = new ControlDimensionSpecification();

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

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			if (invalidCharacterReplacement == 0) {
				invalidCharacterReplacement = ' ';
			}
		}

	}

	public static class ControlDimensionSpecification implements Serializable {

		private static final long serialVersionUID = 1L;

		public int value = 400;
		public ControlSizeUnit unit = ControlSizeUnit.PIXELS;

	}

	public class SingleLineTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		protected SingleLineTextConfiguration controlConfiguration;

		public SingleLineTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				updateControlConfiguration();
			}
			return super.refreshUI(refreshStructure);
		}

		protected void updateControlConfiguration() {
			controlConfiguration = (SingleLineTextConfiguration) loadControlCustomization(input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			JTextComponent result = super.createTextComponent();
			((AbstractDocument) result.getDocument()).setDocumentFilter(new DocumentFilter() {
				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
						throws BadLocationException {
					text = text.replace("\r\n", Character.toString(controlConfiguration.invalidCharacterReplacement));
					text = text.replace("\n", Character.toString(controlConfiguration.invalidCharacterReplacement));
					text = text.replace("\r", Character.toString(controlConfiguration.invalidCharacterReplacement));
					super.replace(fb, offset, length, text, attrs);
				}
			});
			return result;
		}

		@Override
		protected Dimension getScrollPanePreferredSize(Dimension defaultSize) {
			Dimension result = new Dimension(defaultSize);
			if (controlConfiguration.width != null) {
				result.width = controlConfiguration.getWidthInPixels();
			}
			return result;
		}

		@Override
		protected boolean areScrollBarsEnabled() {
			return false;
		}

	}

}
