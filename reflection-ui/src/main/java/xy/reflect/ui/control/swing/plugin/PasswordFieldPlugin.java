


package xy.reflect.ui.control.swing.plugin;

import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

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
					textComponentEditHappened();
				}

			};
		}

		@Override
		protected boolean areScrollBarsEnabled() {
			return false;
		}

		@Override
		protected void updateTextComponent(boolean refreshStructure) {
			super.updateTextComponent(refreshStructure);
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
