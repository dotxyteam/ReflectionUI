
package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.io.Serializable;

import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.plugin.MultipleLinesTextPlugin.MultipleLinesTextConfiguration.ControlSizeUnit;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control plugin that displays password text controls.
 * 
 * @author olitank
 *
 */
public class PasswordFieldPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Password Field";
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
		return new PasswordFieldConfiguration();
	}

	@Override
	public PasswordFieldControl createControl(Object renderer, IFieldControlInput input) {
		return new PasswordFieldControl((SwingRenderer) renderer, input);
	}

	public static class PasswordFieldConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public char echoCharacter = '\u2022';
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
	}

	public static class ControlDimensionSpecification implements Serializable {

		private static final long serialVersionUID = 1L;

		public int value = 400;
		public ControlSizeUnit unit = ControlSizeUnit.PIXELS;

	}

	public class PasswordFieldControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public PasswordFieldControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JTextComponent createTextComponent() {
			return new JPasswordField() {

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
					try {
						PasswordFieldControl.this.textComponentEditHappened();
					} catch (Throwable t) {
						swingRenderer.handleException(PasswordFieldControl.this, t);
					}
				}

			};
		}

		@Override
		protected Dimension getScrollPanePreferredSize(Dimension defaultSize) {
			Dimension result = new Dimension(defaultSize);
			PasswordFieldConfiguration controlCustomization = (PasswordFieldConfiguration) loadControlCustomization(
					input);
			if (controlCustomization.width != null) {
				result.width = controlCustomization.getWidthInPixels();
			}
			return result;
		}

		@Override
		protected boolean areScrollBarsEnabled() {
			return false;
		}

		@Override
		protected void refreshTextComponent(boolean refreshStructure) {
			super.refreshTextComponent(refreshStructure);
			updateTextComponentStyle(refreshStructure);

		}

		protected void updateTextComponentStyle(boolean refreshStructure) {
			if (refreshStructure) {
				PasswordFieldConfiguration controlCustomization = (PasswordFieldConfiguration) loadControlCustomization(
						input);
				((JPasswordField) textComponent).setEchoChar(controlCustomization.echoCharacter);
			}
		}

		@Override
		public String toString() {
			return "PasswordField [data=" + data + "]";
		}
	}

}
